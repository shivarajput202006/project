package com.scamshield.jobfinder.dto;

import java.util.List;

public record JobSearchResponse(
        List<JobResponse> jobs,
        int page,
        int size,
        long total,
        boolean hasMore,
        String sourceName,
        boolean providerAvailable) {
    public JobSearchResponse {
        jobs = List.copyOf(jobs);
    }
}
