package com.scamshield.jobfinder.provider;

import java.util.List;

import com.scamshield.jobfinder.model.Job;

public record JobProviderSearchResult(List<Job> jobs, long total, boolean hasMore) {
    public JobProviderSearchResult {
        jobs = List.copyOf(jobs);
    }
}
