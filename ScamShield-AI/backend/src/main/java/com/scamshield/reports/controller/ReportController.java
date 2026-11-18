package com.scamshield.reports.controller;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import com.scamshield.reports.dto.ReportCategoryResponse;
import com.scamshield.reports.dto.ReportFilterRequest;
import com.scamshield.reports.dto.ReportPageResponse;
import com.scamshield.reports.dto.ReportResponse;
import com.scamshield.reports.dto.ReportSummaryResponse;
import com.scamshield.reports.enums.ReportStatus;
import com.scamshield.reports.enums.ReportType;
import com.scamshield.reports.service.ReportExportService;
import com.scamshield.reports.service.ReportService;
import com.scamshield.security.UserPrincipal;
import com.scamshield.notifications.NotificationEventPublisher;
import com.scamshield.notifications.NotificationType;

@Validated
@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;
    private final ReportExportService exportService;
    private NotificationEventPublisher notificationEventPublisher;

    @Autowired(required = false)
    public void setNotificationEventPublisher(NotificationEventPublisher publisher) {
        this.notificationEventPublisher = publisher;
    }

    public ReportController(ReportService reportService, ReportExportService exportService) {
        this.reportService = reportService;
        this.exportService = exportService;
    }

    @GetMapping
    public ReportPageResponse list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) ReportType type,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) @Size(max = 120) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) @Max(10000) int page,
            @RequestParam(defaultValue = "12") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "NEWEST") ReportFilterRequest.ReportSort sort) {
        return reportService.list(
                principal.getUser().getId(),
                new ReportFilterRequest(type, status, fromDate, toDate, keyword, page, size, sort));
    }

    @GetMapping("/summary")
    public ReportSummaryResponse summary(@AuthenticationPrincipal UserPrincipal principal) {
        return reportService.summary(principal.getUser().getId());
    }

    @GetMapping("/categories")
    public List<ReportCategoryResponse> categories(@AuthenticationPrincipal UserPrincipal principal) {
        return reportService.categories(principal.getUser().getId());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        byte[] pdf = exportService.download(principal.getUser().getId(), id);
        if (notificationEventPublisher != null) {
            notificationEventPublisher.publish(
                    principal.getUser().getId(), "REPORT_EXPORT:" + id, NotificationType.REPORT,
                    "Report exported", "Your requested report PDF is ready.",
                    "/reports");
        }
        String fileId = id.replaceAll("[^A-Za-z0-9_-]", "_");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("scamshield-report-" + fileId + ".pdf").build().toString())
                .body(pdf);
    }

    @GetMapping("/{id}")
    public ReportResponse details(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        return reportService.get(principal.getUser().getId(), id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        reportService.deleteHistory(principal.getUser().getId(), id);
        return ResponseEntity.noContent().build();
    }
}
