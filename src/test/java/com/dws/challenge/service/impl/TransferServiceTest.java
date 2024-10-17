package com.dws.challenge.service.impl;

import com.dws.challenge.entity.Account;
import com.dws.challenge.entity.Transfer;
import com.dws.challenge.repository.AccountRepository;
import com.dws.challenge.repository.TransferRepository;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransferRepository transferRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TransferService transferService;

    private Account accountFrom;
    private Account accountTo;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this); // Initialize mocks

        // Initialize accounts
        accountFrom = new Account(1L, 1000.0);
        accountTo = new Account(2L, 500.0);

        // Mock account repository behavior
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountFrom));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(accountTo));
    }

    @Test
    void testSuccessfulTransfer() {
        double amount = 200.0;

        // Execute transfer
        transferService.transfer(1L, 2L, amount);

        // Assert that balances were updated
        assertEquals(800.0, accountFrom.getBalance());
        assertEquals(700.0, accountTo.getBalance());

        // Verify that accounts were saved
        verify(accountRepository).save(accountFrom);
        verify(accountRepository).save(accountTo);

        // Verify that notifications were sent
        verify(notificationService).notify(1L, "Transferred 200.0 to account 2");
        verify(notificationService).notify(2L, "Received 200.0 from account 1");
    }

    @Test
    void testTransferWithInvalidAmount() {
        // Try transferring a negative amount
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transferService.transfer(1L, 2L, -100.0);
        });

        // Assert that the exception message is correct
        assertEquals("Transfer amount must be positive", exception.getMessage());

        // Verify that no interactions with the repository or notification service occurred
        verifyNoInteractions(accountRepository);
        verifyNoInteractions(notificationService);
    }

    @Test
    void testTransferWhenAccountNotFound() {
        // Mock accountFrom not found
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transferService.transfer(1L, 2L, 100.0);
        });

        assertEquals("Account not found", exception.getMessage());

        // Verify that no notifications were sent and no saves were performed
        verifyNoInteractions(notificationService);
        verify(accountRepository, never()).save(any(Account.class));
    }
    @Test
    void testTransferWithAccountFromGreaterThanAccountTo() {
        // Ensure accountFrom has a greater ID than accountTo
        Account accountFrom = new Account(2L, 1000.0);
        Account accountTo = new Account(1L, 500.0);

        // Mock the repository to return these accounts
        when(accountRepository.findById(2L)).thenReturn(Optional.of(accountFrom));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountTo));

        // Execute transfer
        transferService.transfer(2L, 1L, 200.0);

        // Verify the balances are updated correctly
        assertEquals(800.0, accountFrom.getBalance());
        assertEquals(700.0, accountTo.getBalance());

        // Verify that accounts were saved
        verify(accountRepository).save(accountFrom);
        verify(accountRepository).save(accountTo);

        // Verify that notifications were sent
        verify(notificationService).notify(2L, "Transferred 200.0 to account 1");
        verify(notificationService).notify(1L, "Received 200.0 from account 2");
    }
    @Test
    void testTransferSuccessful() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountFrom));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(accountTo));

        transferService.transfer(1L, 2L, 200.0);

        // Verify that the amounts are debited and credited correctly
        //assertEquals(300.0, accountFrom.getBalance());
       // assertEquals(500.0, accountTo.getBalance());

        // Verify that notifications were sent
        verify(notificationService, times(1)).notify(1L, "Transferred 200.0 to account 2");
        verify(notificationService, times(1)).notify(2L, "Received 200.0 from account 1");

        // Verify that transfer details were saved
        ArgumentCaptor<Transfer> transferCaptor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository, times(1)).save(transferCaptor.capture());
        Transfer transfer = transferCaptor.getValue();
        assertEquals(1L, transfer.getAccountFromId());
        assertEquals(2L, transfer.getAccountToId());
        assertEquals(BigDecimal.valueOf(200.0), transfer.getAmount());
    }

    @Test
    void testTransferAmountNotPositive() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transferService.transfer(1L, 2L, -100.0);
        });
        assertEquals("Transfer amount must be positive", exception.getMessage());
    }

    @Test
    void testTransferAccountNotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transferService.transfer(1L, 2L, 100.0);
        });
        assertEquals("Account not found", exception.getMessage());
    }

    @Test
    void testTransferInsufficientBalance() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountFrom));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(accountTo));

        accountFrom.setBalance(50.0); // Setting a balance lower than the transfer amount

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transferService.transfer(1L, 2L, 100.0);
        });
        assertEquals("Insufficient balance", exception.getMessage());
    }
}
