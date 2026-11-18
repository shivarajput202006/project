package com.scamshield.notifications;

import org.springframework.http.HttpStatus;

public class NotificationException extends RuntimeException {
    private final HttpStatus status;

    public NotificationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }

    public static NotificationException invalid(String message) {
        return new NotificationException(HttpStatus.BAD_REQUEST, message);
    }

    public static NotificationException notFound() {
        return new NotificationException(HttpStatus.NOT_FOUND, "Notification not found.");
    }

    public static NotificationException rateLimited() {
        return new NotificationException(
                HttpStatus.TOO_MANY_REQUESTS, "Too many notification actions. Please wait a minute and try again.");
    }
}
