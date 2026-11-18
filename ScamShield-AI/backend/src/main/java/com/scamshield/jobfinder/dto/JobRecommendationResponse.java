package com.scamshield.jobfinder.dto;

import java.util.List;

public record JobRecommendationResponse(boolean available, List<JobResponse> jobs, String message) {
    public JobRecommendationResponse {
        jobs = List.copyOf(jobs);
    }
}
