package com.scamshield.assistant.exception;

import org.springframework.http.HttpStatus;

public class AssistantException extends RuntimeException {
    private final HttpStatus status;

    public AssistantException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }

    public static AssistantException notFound() {
        return new AssistantException(HttpStatus.NOT_FOUND, "Conversation not found.");
    }

    public static AssistantException invalid(String message) {
        return new AssistantException(HttpStatus.BAD_REQUEST, message);
    }

    public static AssistantException unavailable(String message) {
        return new AssistantException(HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    public static AssistantException rateLimited() {
        return new AssistantException(HttpStatus.TOO_MANY_REQUESTS, "Too many assistant requests. Please wait a minute and try again.");
    }
}
