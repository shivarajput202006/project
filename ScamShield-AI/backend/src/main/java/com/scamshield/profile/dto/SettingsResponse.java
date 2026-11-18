package com.scamshield.profile.dto;

import java.time.Instant;

public record SettingsResponse(
        boolean aiProcessingEnabled,
        String resumeVisibility,
        String aiProcessingExplanation,
        Instant updatedAt) {
}
