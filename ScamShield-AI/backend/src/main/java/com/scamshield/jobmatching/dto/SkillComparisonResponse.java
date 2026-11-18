package com.scamshield.jobmatching.dto;

import java.util.List;

public record SkillComparisonResponse(
        List<String> resumeSkills,
        List<String> matchedListingSkills,
        List<String> missingListingSkills,
        List<String> matchedRequiredSkills,
        List<String> matchedPreferredSkills,
        List<String> missingRequiredSkills,
        List<String> missingPreferredSkills,
        List<String> needsStrongerEvidence,
        String note) {
    public SkillComparisonResponse {
        resumeSkills = List.copyOf(resumeSkills);
        matchedListingSkills = List.copyOf(matchedListingSkills);
        missingListingSkills = List.copyOf(missingListingSkills);
        matchedRequiredSkills = List.copyOf(matchedRequiredSkills);
        matchedPreferredSkills = List.copyOf(matchedPreferredSkills);
        missingRequiredSkills = List.copyOf(missingRequiredSkills);
        missingPreferredSkills = List.copyOf(missingPreferredSkills);
        needsStrongerEvidence = List.copyOf(needsStrongerEvidence);
    }

    public static SkillComparisonResponse unavailable(List<String> resumeSkills, String note) {
        return new SkillComparisonResponse(
                resumeSkills, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), note);
    }
}
