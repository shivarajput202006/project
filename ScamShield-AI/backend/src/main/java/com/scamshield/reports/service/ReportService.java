package com.scamshield.reports.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.scamshield.reports.dto.ReportCategoryResponse;
import com.scamshield.reports.dto.ReportFilterRequest;
import com.scamshield.reports.dto.ReportPageResponse;
import com.scamshield.reports.dto.ReportResponse;
import com.scamshield.reports.dto.ReportSummaryResponse;
import com.scamshield.reports.enums.ReportType;
import com.scamshield.reports.exception.ReportException;

@Service
public class ReportService {
    private final ReportAggregationService aggregationService;
    private final ReportAccessService accessService;

    public ReportService(
            ReportAggregationService aggregationService,
            ReportAccessService accessService) {
        this.aggregationService = aggregationService;
        this.accessService = accessService;
    }

    public ReportPageResponse list(String ownerId, ReportFilterRequest filter) {
        validateDates(filter.fromDate(), filter.toDate());
        var hidden = accessService.hiddenIds(ownerId);
        Comparator<Instant> timestampOrder = filter.sort() == ReportFilterRequest.ReportSort.OLDEST
                ? Comparator.naturalOrder()
                : Comparator.reverseOrder();
        List<ReportResponse> reports = aggregationService.aggregate(ownerId).stream()
                .filter(report -> !hidden.contains(report.id()))
                .filter(report -> filter.type() == null || report.type() == filter.type())
                .filter(report -> filter.status() == null || report.status() == filter.status())
                .filter(report -> inDateRange(report, filter.fromDate(), filter.toDate()))
                .filter(report -> matchesKeyword(report, filter.keyword()))
                .sorted(Comparator.comparing(
                        ReportResponse::createdAt,
                        Comparator.nullsLast(timestampOrder)))
                .toList();

        long offset = (long) (filter.page() - 1) * filter.size();
        int start = (int) Math.min(offset, reports.size());
        int end = Math.min(start + filter.size(), reports.size());
        int totalPages = (int) Math.ceil(reports.size() / (double) filter.size());
        return new ReportPageResponse(
                reports.subList(start, end).stream()
                        .map(ReportResponse::withoutDetails)
                        .toList(),
                filter.page(), filter.size(), reports.size(), totalPages);
    }

    public ReportSummaryResponse summary(String ownerId) {
        return aggregationService.summary(ownerId);
    }

    public List<ReportCategoryResponse> categories(String ownerId) {
        ReportSummaryResponse summary = summary(ownerId);
        return java.util.Arrays.stream(ReportType.values())
                .map(type -> new ReportCategoryResponse(type, type.label(), count(summary, type)))
                .toList();
    }

    public ReportResponse get(String ownerId, String id) {
        return accessService.get(ownerId, id);
    }

    public void deleteHistory(String ownerId, String id) {
        ReportResponse report = accessService.get(ownerId, id);
        accessService.hide(ownerId, report);
    }

    private long count(ReportSummaryResponse summary, ReportType type) {
        return switch (type) {
            case SCAM_DETECTION -> summary.scamDetections();
            case URL_SCAN -> summary.urlScans();
            case RESUME_ANALYSIS -> summary.resumeAnalyses();
            case JOB_MATCHING -> summary.jobMatches();
        };
    }

    private boolean matchesKeyword(ReportResponse report, String keyword) {
        if (keyword == null || keyword.isBlank()) return true;
        String query = keyword.trim().toLowerCase(java.util.Locale.ROOT);
        return report.title().toLowerCase(java.util.Locale.ROOT).contains(query)
                || report.summary().toLowerCase(java.util.Locale.ROOT).contains(query)
                || report.sourceModule().toLowerCase(java.util.Locale.ROOT).contains(query)
                || report.sections().stream()
                        .flatMap(section -> section.items().stream())
                        .anyMatch(item -> item.toLowerCase(java.util.Locale.ROOT).contains(query));
    }

    private boolean inDateRange(ReportResponse report, LocalDate from, LocalDate to) {
        Instant timestamp = report.createdAt();
        if (timestamp == null) return from == null && to == null;
        return (from == null || !timestamp.isBefore(from.atStartOfDay().toInstant(ZoneOffset.UTC)))
                && (to == null || timestamp.isBefore(to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)));
    }

    private void validateDates(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw ReportException.badRequest("fromDate must not be after toDate.");
        }
    }
}
