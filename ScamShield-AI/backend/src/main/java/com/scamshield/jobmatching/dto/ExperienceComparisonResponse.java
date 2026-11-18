package com.scamshield.jobmatching.dto;

import com.scamshield.jobfinder.model.JobExperience;

public record ExperienceComparisonResponse(
        boolean available,
        Integer documentedYears,
        JobExperience listingExperience,
        Integer score,
        String explanation) {
}
