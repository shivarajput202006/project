package com.scamshield.scamdetector.model;

import java.time.Instant;

public record EvidenceItem(
        EvidenceType evidenceType,
        String description,
        String sourceName,
        Instant retrievedAt) {
}
