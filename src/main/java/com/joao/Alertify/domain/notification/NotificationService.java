package com.joao.Alertify.domain.notification;

/**
 * Strategy interface for notification channels.
 */
public interface NotificationService {
    void notify(String message, String recipient);
}

