package com.scamshield.reports.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.scamshield.reports.dto.ReportFilterRequest;
import com.scamshield.reports.dto.ReportResponse;
import com.scamshield.reports.enums.ReportStatus;
import com.scamshield.reports.enums.ReportType;
import com.scamshield.reports.exception.ReportException;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {
    @Mock private ReportAggregationService aggregationService;
    @Mock private ReportAccessService accessService;
    @InjectMocks private ReportService service;
    private List<ReportResponse> reports;

    @BeforeEach
    void setUp() {
        reports = List.of(
                report("SCAM_DETECTION:1", ReportType.SCAM_DETECTION, "Scam analysis", "Advance fee review",
                        Instant.parse("2026-10-03T10:00:00Z"), 70),
                report("URL_SCAN:2", ReportType.URL_SCAN, "URL scan", "example.test reputation",
                        Instant.parse("2026-10-02T10:00:00Z"), 30),
                report("JOB_MATCHING:3", ReportType.JOB_MATCHING, "Job match", "Java Developer role",
                        Instant.parse("2026-10-01T10:00:00Z"), 82));
    }

    @Test
    void appliesTypeKeywordDateSortAndPaginationToActualReports() {
        stubReports();
        var response = service.list("owner", new ReportFilterRequest(
                ReportType.SCAM_DETECTION, ReportStatus.COMPLETED,
                LocalDate.parse("2026-10-03"), LocalDate.parse("2026-10-03"),
                "advance", 1, 5, ReportFilterRequest.ReportSort.NEWEST));
        assertEquals(1, response.totalElements());
        assertEquals("SCAM_DETECTION:1", response.reports().get(0).id());
        assertEquals(List.of(), response.reports().get(0).sections());
    }

    @Test
    void paginatesAndSortsOldestFirst() {
        stubReports();
        var response = service.list("owner", new ReportFilterRequest(
                null, null, null, null, null, 1, 2, ReportFilterRequest.ReportSort.OLDEST));
        assertEquals(3, response.totalElements());
        assertEquals(2, response.reports().size());
        assertEquals("JOB_MATCHING:3", response.reports().get(0).id());
        assertEquals(2, response.totalPages());
    }

    @Test
    void summaryCountsOnlyVisibleSourceRecords() {
        when(aggregationService.summary("owner")).thenReturn(
                new com.scamshield.reports.dto.ReportSummaryResponse(3, 1, 1, 0, 1,
                        Instant.parse("2026-10-03T10:00:00Z")));
        var response = service.summary("owner");
        assertEquals(3, response.totalReports());
        assertEquals(1, response.scamDetections());
        assertEquals(1, response.urlScans());
        assertEquals(0, response.resumeAnalyses());
        assertEquals(1, response.jobMatches());
        assertEquals(Instant.parse("2026-10-03T10:00:00Z"), response.lastActivityAt());
    }

    @Test
    void rejectsReversedDateRange() {
        var filter = new ReportFilterRequest(null, null,
                LocalDate.parse("2026-10-03"), LocalDate.parse("2026-10-01"),
                null, 1, 10, ReportFilterRequest.ReportSort.NEWEST);
        assertEquals(400, assertThrows(ReportException.class, () -> service.list("owner", filter)).getStatus().value());
    }

    private ReportResponse report(
            String id, ReportType type, String title, String summary, Instant createdAt, Integer score) {
        return new ReportResponse(id, id.substring(id.indexOf(':') + 1), type.label(), type, title,
                ReportStatus.COMPLETED, createdAt, summary, score, "Score", List.of());
    }

    private void stubReports() {
        when(aggregationService.aggregate("owner")).thenReturn(reports);
        when(accessService.hiddenIds("owner")).thenReturn(Set.of());
    }
}
