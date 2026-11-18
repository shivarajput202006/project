package com.scamshield.reports.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import com.scamshield.reports.enums.ReportStatus;
import com.scamshield.reports.enums.ReportType;

public record ReportFilterRequest(
        ReportType type,
        ReportStatus status,
        LocalDate fromDate,
        LocalDate toDate,
        @Size(max = 120) String keyword,
        @Min(1) @Max(10000) int page,
        @Min(1) @Max(50) int size,
        ReportSort sort) {
    public enum ReportSort {
        NEWEST,
        OLDEST
    }
}
