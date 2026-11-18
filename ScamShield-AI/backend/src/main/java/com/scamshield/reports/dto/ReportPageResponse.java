package com.scamshield.reports.dto;

import java.util.List;

public record ReportPageResponse(
        List<ReportResponse> reports,
        int page,
        int size,
        long totalElements,
        int totalPages) {
    public ReportPageResponse {
        reports = List.copyOf(reports);
    }
}
