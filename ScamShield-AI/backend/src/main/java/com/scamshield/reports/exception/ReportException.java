package com.scamshield.reports.exception;

import org.springframework.http.HttpStatus;

public class ReportException extends RuntimeException {
    private final HttpStatus status;

    private ReportException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ReportException notFound() {
        return new ReportException(HttpStatus.NOT_FOUND, "Report not found.");
    }

    public static ReportException badRequest(String message) {
        return new ReportException(HttpStatus.BAD_REQUEST, message);
    }
}
