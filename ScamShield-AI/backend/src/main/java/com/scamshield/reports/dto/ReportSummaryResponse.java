package com.scamshield.reports.dto;

import java.time.Instant;

public record ReportSummaryResponse(
        long totalReports,
        long scamDetections,
        long urlScans,
        long resumeAnalyses,
        long jobMatches,
        Instant lastActivityAt) {
}
