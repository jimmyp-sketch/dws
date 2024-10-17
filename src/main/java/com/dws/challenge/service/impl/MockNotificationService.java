package com.dws.challenge.service.impl;

import com.dws.challenge.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MockNotificationService implements NotificationService {

    @Override
    public void notify(Long accountId, String message) {
        // Mock implementation (could log or do nothing)
        log.info("Mock Notification: Account ID: {} {}" , accountId ,message);
    }
}

