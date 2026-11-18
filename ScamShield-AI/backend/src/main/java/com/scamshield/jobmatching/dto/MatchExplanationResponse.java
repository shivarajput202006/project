package com.scamshield.jobmatching.dto;

import java.util.List;

public record MatchExplanationResponse(
        String summary,
        String disclaimer,
        String scoringModelVersion,
        List<MatchCriterionResponse> criteria) {
    public MatchExplanationResponse {
        criteria = List.copyOf(criteria);
    }
}
