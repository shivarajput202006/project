package com.scamshield.admin.dto;

import java.time.Instant;

import com.scamshield.notifications.Notification;
import com.scamshield.notifications.NotificationPriority;
import com.scamshield.notifications.NotificationType;

public record AdminSecurityEventResponse(
        String id, NotificationType type, NotificationPriority priority, Instant createdAt) {
    public static AdminSecurityEventResponse from(Notification notification) {
        return new AdminSecurityEventResponse(
                notification.getId(),
                notification.getType(),
                notification.getPriority(),
                notification.getCreatedAt());
    }
}
