package com.scamshield.reports.dto;

import java.time.Instant;
import java.util.List;

import com.scamshield.reports.enums.ReportStatus;
import com.scamshield.reports.enums.ReportType;

public record ReportResponse(
        String id,
        String sourceRecordId,
        String sourceModule,
        ReportType type,
        String title,
        ReportStatus status,
        Instant createdAt,
        String summary,
        Integer score,
        String scoreLabel,
        List<ReportSectionResponse> sections) {
    public ReportResponse {
        sections = List.copyOf(sections);
    }

    public ReportResponse withoutDetails() {
        return new ReportResponse(
                id, sourceRecordId, sourceModule, type, title, status, createdAt, summary, score, scoreLabel, List.of());
    }
}
