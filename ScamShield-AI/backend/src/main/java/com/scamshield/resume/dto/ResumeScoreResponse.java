package com.scamshield.resume.dto;

import java.util.List;

public record ResumeScoreResponse(
        boolean available,
        Integer overallScore,
        String label,
        List<CriterionScore> criteria,
        String disclaimer) {

    public ResumeScoreResponse {
        criteria = List.copyOf(criteria);
    }

    public record CriterionScore(String name, int score, String explanation, String improvementAction) {
    }
}
