package com.scamshield.jobmatching.service;

import org.springframework.http.HttpStatus;

public class JobMatchingException extends RuntimeException {
    private final HttpStatus status;

    public JobMatchingException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static JobMatchingException notFound() {
        return new JobMatchingException(HttpStatus.NOT_FOUND, "The requested job match was not found.");
    }
}
