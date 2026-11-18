package com.scamshield.jobfinder.dto;

import java.util.List;

public record JobMatchResponse(
        boolean available,
        int score,
        List<String> matchingSkills,
        List<String> missingSkills,
        String explanation) {
    public JobMatchResponse {
        matchingSkills = List.copyOf(matchingSkills);
        missingSkills = List.copyOf(missingSkills);
    }
}
