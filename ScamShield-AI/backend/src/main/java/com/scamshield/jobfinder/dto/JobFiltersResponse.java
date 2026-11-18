package com.scamshield.jobfinder.dto;

import java.util.List;

import com.scamshield.jobfinder.model.JobExperience;
import com.scamshield.jobfinder.model.JobType;
import com.scamshield.jobfinder.model.JobWorkMode;

public record JobFiltersResponse(
        List<JobWorkMode> workModes,
        List<JobExperience> experienceLevels,
        List<JobType> jobTypes,
        boolean salaryFilterAvailable,
        String sourceName,
        boolean providerAvailable,
        String message) {
    public JobFiltersResponse {
        workModes = List.copyOf(workModes);
        experienceLevels = List.copyOf(experienceLevels);
        jobTypes = List.copyOf(jobTypes);
    }
}
