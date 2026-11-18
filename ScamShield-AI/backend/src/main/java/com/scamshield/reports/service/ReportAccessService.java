package com.scamshield.reports.service;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.scamshield.reports.dto.ReportResponse;
import com.scamshield.reports.exception.ReportException;
import com.scamshield.reports.model.ReportMetadata;
import com.scamshield.reports.repository.ReportRepository;

@Service
public class ReportAccessService {
    private final ReportAggregationService aggregationService;
    private final ReportRepository reportRepository;

    public ReportAccessService(
            ReportAggregationService aggregationService,
            ReportRepository reportRepository) {
        this.aggregationService = aggregationService;
        this.reportRepository = reportRepository;
    }

    public ReportResponse get(String ownerId, String reportId) {
        Set<String> hiddenIds = hiddenIds(ownerId);
        return aggregationService.find(ownerId, reportId)
                .filter(report -> !hiddenIds.contains(report.id()))
                .orElseThrow(ReportException::notFound);
    }

    public void hide(String ownerId, ReportResponse report) {
        ReportMetadata metadata = reportRepository
                .findByOwnerIdAndSourceModuleAndSourceRecordId(
                        ownerId, report.sourceModule(), report.sourceRecordId())
                .orElseGet(() -> new ReportMetadata(
                        ownerId,
                        report.sourceModule(),
                        report.sourceRecordId(),
                        report.title(),
                        report.type(),
                        report.status(),
                        report.createdAt(),
                        Instant.now()));
        if (metadata.getHiddenAt() == null) {
            metadata.hideAt(Instant.now());
        }
        reportRepository.save(metadata);
    }

    public Set<String> hiddenIds(String ownerId) {
        return reportRepository.findByOwnerIdAndHiddenAtIsNotNull(ownerId).stream()
                .map(metadata -> metadata.getType().name() + ":" + metadata.getSourceRecordId())
                .collect(Collectors.toUnmodifiableSet());
    }
}
