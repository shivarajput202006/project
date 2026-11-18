package com.scamshield.reports.dto;

import java.util.List;

public record ReportSectionResponse(String title, List<String> items) {
    public ReportSectionResponse {
        items = List.copyOf(items);
    }
}
