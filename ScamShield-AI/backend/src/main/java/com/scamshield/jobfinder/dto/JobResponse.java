package com.scamshield.jobfinder.dto;

import java.time.Instant;
import java.util.List;

import com.scamshield.jobfinder.model.Job;
import com.scamshield.jobfinder.model.JobExperience;
import com.scamshield.jobfinder.model.JobType;
import com.scamshield.jobfinder.model.JobVerificationStatus;
import com.scamshield.jobfinder.model.JobWorkMode;

public record JobResponse(
        String id,
        String sourceName,
        String sourceJobId,
        String title,
        String company,
        String location,
        JobWorkMode workMode,
        JobExperience experience,
        JobType jobType,
        List<String> requiredSkills,
        String description,
        Double salaryMin,
        Double salaryMax,
        String salaryCurrency,
        boolean salaryIsPredicted,
        Instant postedAt,
        Instant fetchedAt,
        Instant lastUpdatedAt,
        JobVerificationStatus verificationStatus,
        String providerReference,
        String officialApplyUrl) {
    public JobResponse {
        requiredSkills = List.copyOf(requiredSkills);
    }

    public static JobResponse from(Job job) {
        return new JobResponse(
                job.getId(),
                job.getSourceName(),
                job.getSourceJobId(),
                job.getTitle(),
                job.getCompany(),
                job.getLocation(),
                job.getWorkMode(),
                job.getExperience(),
                job.getJobType(),
                job.getRequiredSkills(),
                job.getDescription(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getSalaryCurrency(),
                job.isSalaryIsPredicted(),
                job.getPostedAt(),
                job.getFetchedAt(),
                job.getLastUpdatedAt(),
                job.getVerificationStatus(),
                job.getProviderReference(),
                job.getOfficialApplyUrl());
    }
}
