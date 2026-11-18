package com.scamshield.resume.dto;

import java.util.List;

public record ResumeRecommendationResponse(
        String resumeId,
        List<ExtractedResumeData.RecommendedRole> roles,
        boolean liveSearchConfigured,
        boolean liveListingsIncluded,
        String message) {

    public ResumeRecommendationResponse {
        roles = List.copyOf(roles);
    }
}
