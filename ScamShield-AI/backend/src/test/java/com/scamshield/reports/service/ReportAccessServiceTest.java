package com.scamshield.reports.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.scamshield.reports.dto.ReportResponse;
import com.scamshield.reports.enums.ReportStatus;
import com.scamshield.reports.enums.ReportType;
import com.scamshield.reports.exception.ReportException;
import com.scamshield.reports.model.ReportMetadata;
import com.scamshield.reports.repository.ReportRepository;

@ExtendWith(MockitoExtension.class)
class ReportAccessServiceTest {
    @Mock private ReportAggregationService aggregationService;
    @Mock private ReportRepository reportRepository;
    @InjectMocks private ReportAccessService service;

    @Test
    void onlyResolvesAReportPresentInTheAuthenticatedOwnersSourceRecords() {
        when(reportRepository.findByOwnerIdAndHiddenAtIsNotNull("owner")).thenReturn(List.of());
        when(aggregationService.find("owner", "SCAM_DETECTION:owned"))
                .thenReturn(java.util.Optional.of(report("SCAM_DETECTION:owned")));
        assertEquals("SCAM_DETECTION:owned", service.get("owner", "SCAM_DETECTION:owned").id());
        assertThrows(ReportException.class, () -> service.get("other-owner", "SCAM_DETECTION:owned"));
    }

    @Test
    void deletedSourceRecordIsNotRecreatedFromItsHistoryMetadata() {
        when(reportRepository.findByOwnerIdAndHiddenAtIsNotNull("owner")).thenReturn(List.of());
        when(aggregationService.find("owner", "URL_SCAN:deleted")).thenReturn(java.util.Optional.empty());
        assertThrows(ReportException.class, () -> service.get("owner", "URL_SCAN:deleted"));
    }

    @Test
    void hidesOnlyTheHistoryReferenceWithoutDeletingTheSourceRecord() {
        ReportResponse report = report("URL_SCAN:scan-id");
        when(reportRepository.findByOwnerIdAndHiddenAtIsNotNull("owner")).thenReturn(List.of());
        when(aggregationService.find("owner", report.id())).thenReturn(java.util.Optional.of(report));
        when(reportRepository.findByOwnerIdAndSourceModuleAndSourceRecordId("owner", "Smart URL Scanner", "scan-id"))
                .thenReturn(java.util.Optional.empty());
        service.hide("owner", service.get("owner", report.id()));
        ArgumentCaptor<ReportMetadata> saved = ArgumentCaptor.forClass(ReportMetadata.class);
        verify(reportRepository).save(saved.capture());
        assertNotNull(saved.getValue().getHiddenAt());
        assertEquals("scan-id", saved.getValue().getSourceRecordId());
    }

    @Test
    void hiddenReportCannotBeReadOrExported() {
        ReportMetadata hidden = new ReportMetadata("owner", "Smart URL Scanner", "scan-id", "URL scan",
                ReportType.URL_SCAN, ReportStatus.COMPLETED, Instant.now(), Instant.now());
        when(reportRepository.findByOwnerIdAndHiddenAtIsNotNull("owner")).thenReturn(List.of(hidden));
        when(aggregationService.find("owner", "URL_SCAN:scan-id"))
                .thenReturn(java.util.Optional.of(report("URL_SCAN:scan-id")));
        assertThrows(ReportException.class, () -> service.get("owner", "URL_SCAN:scan-id"));
    }

    private ReportResponse report(String id) {
        return new ReportResponse(id, id.substring(id.indexOf(':') + 1), "Smart URL Scanner",
                ReportType.URL_SCAN, "URL scan", ReportStatus.COMPLETED, Instant.now(),
                "Risk assessment", 20, "Risk score", List.of());
    }
}
