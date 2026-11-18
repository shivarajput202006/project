package com.scamshield.jobmatching.dto;

public record MatchCriterionResponse(
        String criterion,
        int configuredWeight,
        Integer score,
        int effectiveWeight,
        String explanation) {
}
