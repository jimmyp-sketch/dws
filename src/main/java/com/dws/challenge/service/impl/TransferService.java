package com.dws.challenge.service.impl;

import com.dws.challenge.entity.Account;
import com.dws.challenge.entity.Transfer;
import com.dws.challenge.repository.AccountRepository;
import com.dws.challenge.repository.TransferRepository;
import com.dws.challenge.service.NotificationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final NotificationService notificationService;
    private final TransferRepository transferRepository;

    @Autowired
    public TransferService(AccountRepository accountRepository, NotificationService notificationService, TransferRepository transferRepository) {
        this.accountRepository = accountRepository;
        this.notificationService = notificationService;
        this.transferRepository = transferRepository;
    }

    @Transactional
    public void transfer(Long accountFromId, Long accountToId, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        Account accountFrom = accountRepository.findById(accountFromId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        Account accountTo = accountRepository.findById(accountToId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // To prevent deadlock, lock accounts in a consistent order
        if (accountFrom.getId() < accountTo.getId()) {
            synchronized (accountFrom) {
                synchronized (accountTo) {
                    performTransfer(accountFrom, accountTo, amount);
                }
            }
        } else {
            synchronized (accountTo) {
                synchronized (accountFrom) {
                    performTransfer(accountFrom, accountTo, amount);
                }
            }
        }
        // Save transfer details
        saveTransferDetails(accountFrom.getId(), accountTo.getId(), amount);

    }

    private void performTransfer(Account accountFrom, Account accountTo, double amount) {
        accountFrom.debit(amount);
        accountTo.credit(amount);

        // Save both accounts
        accountRepository.save(accountFrom);
        accountRepository.save(accountTo);

        // Notify both account holders
        if (notificationService != null) {
            notificationService.notify(accountFrom.getId(), "Transferred " + amount + " to account " + accountTo.getId());
            notificationService.notify(accountTo.getId(), "Received " + amount + " from account " + accountFrom.getId());

        }
    }

    private void saveTransferDetails(Long accountFromId, Long accountToId, double amount) {
        Transfer transfer = new Transfer();
        transfer.setAccountFromId(accountFromId);
        transfer.setAccountToId(accountToId);
        transfer.setAmount(BigDecimal.valueOf(amount));
        transfer.setTransferDate(OffsetDateTime.now());
        transferRepository.save(transfer);
    }
}

