package com.dws.challenge.service;

import org.springframework.stereotype.Service;

public interface NotificationService {
    void notify(Long accountId, String message);
}
