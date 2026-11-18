package com.scamshield.jobfinder.provider;

import java.util.Optional;

import com.scamshield.jobfinder.dto.JobSearchRequest;
import com.scamshield.jobfinder.model.Job;

public interface JobProvider {
    String sourceName();

    String sourceUrl();

    boolean isConfigured();

    JobProviderSearchResult search(JobSearchRequest request);

    Optional<Job> findById(String sourceJobId, String keyword, String location);
}
