package com.scamshield.resume.dto;

import java.time.Instant;
import java.util.List;

import com.scamshield.resume.model.ResumeAnalysis;

public record ResumeAnalysisResponse(
        String id,
        String resumeId,
        ExtractedResumeData extractedData,
        SkillAnalysisResponse skillAnalysis,
        ResumeScoreResponse score,
        List<String> atsFeedback,
        List<ExtractedResumeData.ImprovementSuggestion> improvementSuggestions,
        List<ExtractedResumeData.RecommendedRole> recommendedRoles,
        Instant analysisTimestamp,
        List<String> limitations) {

    public ResumeAnalysisResponse {
        atsFeedback = List.copyOf(atsFeedback);
        improvementSuggestions = List.copyOf(improvementSuggestions);
        recommendedRoles = List.copyOf(recommendedRoles);
        limitations = List.copyOf(limitations);
    }

    public static ResumeAnalysisResponse from(ResumeAnalysis analysis) {
        return new ResumeAnalysisResponse(
                analysis.getId(),
                analysis.getResumeId(),
                analysis.getExtractedData(),
                analysis.getSkillAnalysis(),
                analysis.getScore(),
                analysis.getAtsFeedback(),
                analysis.getImprovementSuggestions(),
                analysis.getRecommendedRoles(),
                analysis.getAnalysisTimestamp(),
                analysis.getLimitations());
    }
}
