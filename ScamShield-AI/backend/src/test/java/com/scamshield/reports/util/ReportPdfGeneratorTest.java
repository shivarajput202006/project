package com.scamshield.reports.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import com.scamshield.reports.dto.ReportResponse;
import com.scamshield.reports.dto.ReportSectionResponse;
import com.scamshield.reports.enums.ReportStatus;
import com.scamshield.reports.enums.ReportType;

class ReportPdfGeneratorTest {
    private final ReportPdfGenerator generator = new ReportPdfGenerator();

    @Test
    void createsReadablePdfWithBrandingActualFindingsAndCategoryDisclaimer() throws Exception {
        ReportResponse report = new ReportResponse(
                "SCAM_DETECTION:report-1",
                "report-1",
                "AI Scam Detector",
                ReportType.SCAM_DETECTION,
                "Scam analysis",
                ReportStatus.COMPLETED,
                Instant.parse("2026-10-01T10:00:00Z"),
                "The offer requested an advance fee.",
                75,
                "Risk score",
                List.of(new ReportSectionResponse("Evidence", List.of("Requested payment before an interview."))));
        try (var document = Loader.loadPDF(generator.generate(report))) {
            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains("SCAMSHIELD AI"));
            assertTrue(text.contains("advance fee"));
            assertTrue(text.contains("Requested payment"));
            assertTrue(text.contains("Automated risk assessments"));
        }
    }
}
