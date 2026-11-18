package com.scamshield.notifications.event;

import com.scamshield.notifications.NotificationPriority;
import com.scamshield.notifications.NotificationType;

public record NotificationEvent(
        String userId,
        String eventKey,
        NotificationType type,
        NotificationPriority priority,
        String title,
        String message,
        String actionUrl) {
}
