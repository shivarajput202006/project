package com.scamshield.notifications;

import com.scamshield.notifications.dto.NotificationResponse;

public final class NotificationMapper {
    private NotificationMapper() {
    }

    public static NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.from(notification);
    }
}
