package com.scamshield.jobmatching.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.scamshield.jobfinder.model.JobType;
import com.scamshield.jobfinder.model.JobWorkMode;

public record JobMatchRequest(
        @NotBlank @Size(max = 128) String resumeId,
        @Size(max = 120) String keyword,
        @Size(max = 100) String location,
        JobWorkMode workMode,
        JobType jobType,
        @Min(1) @Max(10000) int page,
        @Min(1) @Max(50) int size) {
}
