package com.scamshield.jobmatching.dto;

import java.util.List;

public record JobMatchSearchResponse(
        boolean available,
        String resumeId,
        List<JobMatchResponse> matches,
        int page,
        int size,
        long total,
        boolean hasMore,
        String sourceName,
        String message,
        String scoringModelVersion) {
    public JobMatchSearchResponse {
        matches = List.copyOf(matches);
    }
}
