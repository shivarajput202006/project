package com.scamshield.profile.util;

import org.springframework.http.HttpStatus;

public class ProfileException extends RuntimeException {
    private final HttpStatus status;

    public ProfileException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ProfileException badRequest(String message) {
        return new ProfileException(HttpStatus.BAD_REQUEST, message);
    }

    public static ProfileException unauthorized(String message) {
        return new ProfileException(HttpStatus.UNAUTHORIZED, message);
    }

    public static ProfileException notFound(String message) {
        return new ProfileException(HttpStatus.NOT_FOUND, message);
    }
}
