package com.scamshield.jobmatching.dto;

public record EducationComparisonResponse(
        boolean available,
        String resumeEvidence,
        String listingRequirement,
        Integer score,
        String explanation) {
}
