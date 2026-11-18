package com.scamshield.scamdetector.exception;

import org.springframework.http.HttpStatus;

public class ScamDetectorException extends RuntimeException {
    private final HttpStatus status;

    public ScamDetectorException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ScamDetectorException badRequest(String message) {
        return new ScamDetectorException(HttpStatus.BAD_REQUEST, message);
    }

    public static ScamDetectorException unavailable(String message) {
        return new ScamDetectorException(HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    public static ScamDetectorException invalidAiResponse() {
        return new ScamDetectorException(
                HttpStatus.BAD_GATEWAY,
                "The AI service returned a response we could not validate. Please try again.");
    }

    public static ScamDetectorException notFound() {
        return new ScamDetectorException(HttpStatus.NOT_FOUND, "Analysis report was not found.");
    }
}
