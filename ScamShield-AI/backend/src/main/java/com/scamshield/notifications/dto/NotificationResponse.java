package com.scamshield.notifications.dto;

import java.time.Instant;

import com.scamshield.notifications.Notification;
import com.scamshield.notifications.NotificationPriority;
import com.scamshield.notifications.NotificationType;

public record NotificationResponse(
        String id,
        String title,
        String message,
        NotificationType type,
        NotificationPriority priority,
        boolean isRead,
        String actionUrl,
        Instant createdAt,
        Instant readAt) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getPriority(),
                notification.isRead(),
                notification.getActionUrl(),
                notification.getCreatedAt(),
                notification.getReadAt());
    }
}
