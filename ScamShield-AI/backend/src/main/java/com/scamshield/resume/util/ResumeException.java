package com.scamshield.resume.util;

import org.springframework.http.HttpStatus;

public class ResumeException extends RuntimeException {
    private final HttpStatus status;

    public ResumeException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ResumeException badRequest(String message) {
        return new ResumeException(HttpStatus.BAD_REQUEST, message);
    }

    public static ResumeException unavailable(String message) {
        return new ResumeException(HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    public static ResumeException invalidAiResponse() {
        return new ResumeException(
                HttpStatus.BAD_GATEWAY,
                "The AI service returned analysis we could not validate. Please retry.");
    }

    public static ResumeException notFound() {
        return new ResumeException(HttpStatus.NOT_FOUND, "Resume was not found.");
    }

    public static ResumeException analysisNotFound() {
        return new ResumeException(HttpStatus.NOT_FOUND, "Resume analysis was not found.");
    }
}
