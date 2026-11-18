package com.scamshield.reports.dto;

import com.scamshield.reports.enums.ReportType;

public record ReportCategoryResponse(ReportType type, String label, long count) {
}
