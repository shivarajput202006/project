package com.scamshield.scamdetector.model;

public record RiskIndicator(
        String category,
        String title,
        String details,
        EvidenceType evidenceType) {
}
