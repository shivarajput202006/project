package com.scamshield.reports.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.scamshield.jobmatching.dto.MatchCriterionResponse;
import com.scamshield.jobmatching.dto.MatchExplanationResponse;
import com.scamshield.jobmatching.model.JobMatch;
import com.scamshield.jobmatching.repository.JobMatchRepository;
import com.scamshield.resume.dto.ExtractedResumeData;
import com.scamshield.resume.dto.ResumeScoreResponse;
import com.scamshield.resume.dto.SkillAnalysisResponse;
import com.scamshield.resume.model.ResumeAnalysis;
import com.scamshield.resume.repository.ResumeAnalysisRepository;
import com.scamshield.reports.enums.ReportType;
import com.scamshield.reports.dto.ReportSummaryResponse;
import com.scamshield.reports.repository.ReportRepository;
import com.scamshield.scamdetector.model.AiAnalysisResult;
import com.scamshield.scamdetector.model.EvidenceItem;
import com.scamshield.scamdetector.model.EvidenceType;
import com.scamshield.scamdetector.model.InputType;
import com.scamshield.scamdetector.model.RiskIndicator;
import com.scamshield.scamdetector.model.RiskStatus;
import com.scamshield.scamdetector.model.ScamAnalysis;
import com.scamshield.scamdetector.repository.ScamAnalysisRepository;
import com.scamshield.urlscanner.URLRiskIndicator;
import com.scamshield.urlscanner.URLRiskStatus;
import com.scamshield.urlscanner.URLScan;
import com.scamshield.urlscanner.URLScanRepository;

@ExtendWith(MockitoExtension.class)
class ReportAggregationServiceTest {
    @Mock private ScamAnalysisRepository scamRepository;
    @Mock private URLScanRepository urlRepository;
    @Mock private ResumeAnalysisRepository resumeRepository;
    @Mock private JobMatchRepository jobMatchRepository;
    @Mock private ReportRepository reportRepository;
    @InjectMocks private ReportAggregationService service;

    @Test
    void aggregatesOwnerScopedRecordsFromAllFourExistingModulesWithoutCopyingResumeContactInfo() {
        Instant at = Instant.parse("2026-10-01T10:00:00Z");
        ScamAnalysis scam = new ScamAnalysis("owner", InputType.TEXT, "Suspicious offer",
                new AiAnalysisResult(RiskStatus.NEEDS_REVIEW, 72, "Review this offer.",
                        List.of(new RiskIndicator("payment", "Advance fee", "Requests a payment", EvidenceType.USER_PROVIDED)),
                        List.of(new EvidenceItem(EvidenceType.USER_PROVIDED, "Payment request", "User text", at)),
                        "UNVERIFIED", List.of("Verify the employer independently."), List.of()), at);
        scam.setId("scam-id");
        URLScan url = new URLScan("owner", "https://example.test", "example.test", "https", "DOMAIN",
                URLRiskStatus.NEEDS_REVIEW, 34, List.of(new URLRiskIndicator("shortener", "Shortened URL",
                        "Redirect chain could not be verified.", "MEDIUM", true)),
                null, null, List.of("Check the final destination."), at);
        url.setId("url-id");
        var extracted = new ExtractedResumeData(
                new ExtractedResumeData.PersonalInfo("Private Name", "private@example.test", "123", null, null, null),
                List.of(new ExtractedResumeData.Education("BSc", "Example University", "Computing", null, null, null)),
                List.of("Java"), List.of("Communication"),
                List.of(new ExtractedResumeData.Experience("Work", "Developer", "Example Co", null, null, null, "Built services")),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        ResumeAnalysis resume = new ResumeAnalysis("resume-id", "owner", extracted,
                new SkillAnalysisResponse(List.of("Java"), List.of("Communication"), List.of()),
                new ResumeScoreResponse(true, 78, "Developing", List.of(), "Advisory only."), at, List.of());
        resume.setId("resume-analysis-id");
        JobMatch match = new JobMatch("owner", "resume-id", "Adzuna", "provider-job-1",
                "Java Developer", "London", null, null, 81,
                List.of(new MatchCriterionResponse("Technical skills", 50, 80, 50, "Evidence matches listing terms.")),
                List.of("Java"), List.of("React"),
                new MatchExplanationResponse("Relevant listing.", "Not a hiring probability.", "v1", List.of()),
                "v1", at);
        match.setId("match-id");

        when(scamRepository.findByUserIdOrderByAnalysisTimestampDesc("owner")).thenReturn(List.of(scam));
        when(urlRepository.findByUserIdOrderByScanTimestampDesc("owner")).thenReturn(List.of(url));
        when(resumeRepository.findByOwnerIdOrderByAnalysisTimestampDesc("owner")).thenReturn(List.of(resume));
        when(jobMatchRepository.findByOwnerIdOrderByAnalyzedAtDesc("owner")).thenReturn(List.of(match));

        var reports = service.aggregate("owner");

        assertEquals(4, reports.size());
        assertEquals(List.of(ReportType.SCAM_DETECTION, ReportType.URL_SCAN,
                ReportType.RESUME_ANALYSIS, ReportType.JOB_MATCHING),
                reports.stream().map(report -> report.type()).toList());
        assertFalse(reports.get(2).sections().toString().contains("Private Name"));
        assertEquals(81, reports.get(3).score());
        verify(scamRepository).findByUserIdOrderByAnalysisTimestampDesc("owner");
        verify(urlRepository).findByUserIdOrderByScanTimestampDesc("owner");
        verify(resumeRepository).findByOwnerIdOrderByAnalysisTimestampDesc("owner");
        verify(jobMatchRepository).findByOwnerIdOrderByAnalyzedAtDesc("owner");
    }

    @Test
    void emptySourceHistoriesProduceNoFabricatedReports() {
        when(scamRepository.findByUserIdOrderByAnalysisTimestampDesc("owner")).thenReturn(List.of());
        when(urlRepository.findByUserIdOrderByScanTimestampDesc("owner")).thenReturn(List.of());
        when(resumeRepository.findByOwnerIdOrderByAnalysisTimestampDesc("owner")).thenReturn(List.of());
        when(jobMatchRepository.findByOwnerIdOrderByAnalyzedAtDesc("owner")).thenReturn(List.of());
        assertEquals(List.of(), service.aggregate("owner"));
    }

    @Test
    void summaryCountsOwnerRecordsAndSubtractsHiddenHistoryEntries() {
        when(scamRepository.countByUserId("owner")).thenReturn(2L);
        when(urlRepository.countByUserId("owner")).thenReturn(3L);
        when(resumeRepository.countByOwnerId("owner")).thenReturn(4L);
        when(jobMatchRepository.countByOwnerId("owner")).thenReturn(5L);
        when(reportRepository.countByOwnerIdAndTypeAndHiddenAtIsNotNull("owner", ReportType.SCAM_DETECTION)).thenReturn(1L);
        when(reportRepository.countByOwnerIdAndTypeAndHiddenAtIsNotNull("owner", ReportType.URL_SCAN)).thenReturn(1L);
        when(reportRepository.countByOwnerIdAndTypeAndHiddenAtIsNotNull("owner", ReportType.RESUME_ANALYSIS)).thenReturn(1L);
        when(reportRepository.countByOwnerIdAndTypeAndHiddenAtIsNotNull("owner", ReportType.JOB_MATCHING)).thenReturn(1L);
        when(scamRepository.findTop1ByUserIdOrderByAnalysisTimestampDesc("owner")).thenReturn(Optional.empty());
        when(urlRepository.findTop1ByUserIdOrderByScanTimestampDesc("owner")).thenReturn(Optional.empty());
        when(resumeRepository.findFirstByOwnerIdOrderByAnalysisTimestampDesc("owner")).thenReturn(Optional.empty());
        when(jobMatchRepository.findFirstByOwnerIdOrderByAnalyzedAtDesc("owner")).thenReturn(Optional.empty());

        ReportSummaryResponse summary = service.summary("owner");

        assertEquals(10, summary.totalReports());
        assertEquals(1, summary.scamDetections());
        assertEquals(2, summary.urlScans());
        assertEquals(3, summary.resumeAnalyses());
        assertEquals(4, summary.jobMatches());
        assertNull(summary.lastActivityAt());
    }
}
