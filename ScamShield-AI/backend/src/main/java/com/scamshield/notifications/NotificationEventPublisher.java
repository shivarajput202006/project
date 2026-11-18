package com.scamshield.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.scamshield.notifications.event.NotificationEvent;

@Component
public class NotificationEventPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventPublisher.class);
    private final ApplicationEventPublisher publisher;

    public NotificationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(
            String userId,
            String eventKey,
            NotificationType type,
            String title,
            String message,
            String actionUrl) {
        try {
            publisher.publishEvent(new NotificationEvent(
                    userId, eventKey, type,
                    type == NotificationType.SECURITY ? NotificationPriority.HIGH : NotificationPriority.NORMAL,
                    title, message, actionUrl));
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not publish notification event of type {}", type);
        }
    }
}
