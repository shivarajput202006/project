package com.scamshield.admin.dto;

public record AdminMetricsResponse(
        long totalUsers,
        Long activeUsers,
        long scamAnalyses,
        long urlScans,
        long resumeAnalyses,
        Long jobSearches,
        long jobMatchAnalyses,
        long storedJobListings) {
}
