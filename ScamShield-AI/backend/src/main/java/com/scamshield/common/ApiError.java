package com.scamshield.common;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors,
        String path,
        Instant timestamp) {

    public static ApiError of(
            int status, String error, String message, Map<String, String> fieldErrors, String path) {
        return new ApiError(status, error, message, fieldErrors, path, Instant.now());
    }
}
