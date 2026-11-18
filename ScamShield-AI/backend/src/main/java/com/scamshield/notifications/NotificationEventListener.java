package com.scamshield.notifications;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.scamshield.notifications.event.NotificationEvent;

@Component
public class NotificationEventListener {
    private final NotificationCreationService creationService;

    public NotificationEventListener(NotificationCreationService creationService) {
        this.creationService = creationService;
    }

    @EventListener
    public void onNotificationEvent(NotificationEvent event) {
        creationService.createFromEvent(event);
    }
}
