package com.scamshield.jobfinder.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import com.scamshield.jobfinder.model.JobExperience;
import com.scamshield.jobfinder.model.JobType;
import com.scamshield.jobfinder.model.JobWorkMode;

public record JobSearchRequest(
        @Size(max = 120) String keyword,
        @Size(max = 100) String location,
        JobWorkMode workMode,
        JobExperience experience,
        JobType jobType,
        @Min(0) @Max(10000000) Integer salaryMin,
        @Min(1) @Max(10000) int page,
        @Min(1) @Max(50) int size) {
}
