package com.scamshield.notifications.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.scamshield.notifications.NotificationPriority;
import com.scamshield.notifications.NotificationType;

public record NotificationRequest(
        @NotBlank @Size(max = 100) String title,
        @NotBlank @Size(max = 500) String message,
        @NotNull NotificationType type,
        @NotNull NotificationPriority priority,
        @Size(max = 200) String actionUrl) {
}
