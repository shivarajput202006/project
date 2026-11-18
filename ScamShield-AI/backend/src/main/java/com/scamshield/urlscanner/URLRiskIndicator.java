package com.scamshield.urlscanner;

public record URLRiskIndicator(
        String code,
        String title,
        String description,
        String severity,
        boolean heuristic) {
}
