package com.scamshield.jobmatching.dto;

import java.time.Instant;
import java.util.List;

import com.scamshield.jobfinder.dto.JobResponse;
import com.scamshield.jobmatching.enums.MatchStatus;

public record JobMatchResponse(
        String id,
        String resumeId,
        String sourceName,
        String sourceJobId,
        MatchStatus status,
        JobResponse job,
        Integer score,
        List<MatchCriterionResponse> scoreBreakdown,
        SkillComparisonResponse skills,
        ExperienceComparisonResponse experience,
        EducationComparisonResponse education,
        String locationCompatibility,
        String jobTypeCompatibility,
        MatchExplanationResponse explanation,
        List<SkillRoadmapItem> roadmap,
        Instant analyzedAt) {
    public JobMatchResponse {
        scoreBreakdown = List.copyOf(scoreBreakdown);
        roadmap = List.copyOf(roadmap);
    }
}
