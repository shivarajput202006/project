package com.scamshield.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.scamshield.scamdetector.model.AiAnalysisResult;
import com.scamshield.scamdetector.model.EvidenceItem;
import com.scamshield.scamdetector.model.InputType;
import com.scamshield.scamdetector.model.RiskStatus;
import com.scamshield.scamdetector.model.ScamAnalysis;
import com.scamshield.scamdetector.repository.ScamAnalysisRepository;
import com.scamshield.security.UserPrincipal;
import com.scamshield.user.User;
import com.scamshield.user.UserRole;
import com.scamshield.urlscanner.URLScanRepository;
import com.scamshield.urlscanner.URLScan;
import com.scamshield.urlscanner.URLRiskStatus;
import com.scamshield.jobfinder.repository.JobRepository;
import com.scamshield.jobfinder.repository.SavedJobRepository;
import com.scamshield.resume.repository.ResumeAnalysisRepository;
import com.scamshield.resume.dto.ExtractedResumeData;
import com.scamshield.resume.dto.ResumeScoreResponse;
import com.scamshield.resume.dto.SkillAnalysisResponse;
import com.scamshield.resume.model.ResumeAnalysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    @Mock
    private ScamAnalysisRepository repository;

    @Mock
    private URLScanRepository urlScanRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private SavedJobRepository savedJobRepository;

    @Mock
    private ResumeAnalysisRepository resumeAnalysisRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardService(
                repository, urlScanRepository, jobRepository, savedJobRepository, resumeAnalysisRepository);
    }

    @Test
    void overviewUsesAuthenticatedUserAndReturnsEmptyUnimplementedFeatureData() {
        User authenticatedUser = new User(
                "Morgan Lee",
                "morgan@example.com",
                "password-hash",
                UserRole.USER,
                Instant.parse("2026-01-01T00:00:00Z"));
        authenticatedUser.setId("authenticated-user");
        when(repository.countByUserId("authenticated-user")).thenReturn(0L);
        when(repository.findTop5ByUserIdOrderByAnalysisTimestampDesc("authenticated-user")).thenReturn(List.of());
        when(urlScanRepository.countByUserId("authenticated-user")).thenReturn(0L);
        when(urlScanRepository.findTop5ByUserIdOrderByScanTimestampDesc("authenticated-user")).thenReturn(List.of());
        when(savedJobRepository.countByUserId("authenticated-user")).thenReturn(0L);
        when(savedJobRepository.findTop5ByUserIdOrderBySavedAtDesc("authenticated-user")).thenReturn(List.of());

        DashboardResponse response = dashboardService.getOverview(new UserPrincipal(authenticatedUser));

        assertEquals("Morgan Lee", response.user().fullName());
        assertEquals("morgan@example.com", response.user().email());
        assertEquals(DashboardStats.empty(), response.stats());
        assertEquals(0, response.stats().totalScamAnalyses());
        assertEquals(0, response.stats().urlsScanned());
        assertEquals(0, response.stats().savedJobs());
        assertEquals(0, response.stats().resumeAnalyses());
        assertEquals(0, response.stats().reportsSubmitted());
        assertEquals(0, response.recentActivities().size());
        assertEquals(0, response.savedJobs().size());
        assertEquals(0, response.notifications().size());
        verify(repository).countByUserId("authenticated-user");
        verify(repository).findTop5ByUserIdOrderByAnalysisTimestampDesc("authenticated-user");
        verify(urlScanRepository).countByUserId("authenticated-user");
        verify(urlScanRepository).findTop5ByUserIdOrderByScanTimestampDesc("authenticated-user");
        verify(savedJobRepository).countByUserId("authenticated-user");
    }

    @Test
    void dashboardCountsAndShowsOnlyTheAuthenticatedUsersScamAnalyses() {
        User authenticatedUser = new User(
                "Morgan Lee",
                "morgan@example.com",
                "password-hash",
                UserRole.USER,
                Instant.parse("2026-01-01T00:00:00Z"));
        authenticatedUser.setId("authenticated-user");
        ScamAnalysis analysis = new ScamAnalysis(
                "authenticated-user",
                InputType.TEXT,
                "Text message review",
                new AiAnalysisResult(
                        RiskStatus.HIGH_RISK,
                        82,
                        "A payment request was identified.",
                        List.of(),
                        List.of(new EvidenceItem(
                                com.scamshield.scamdetector.model.EvidenceType.AI_INFERRED,
                                "The message requests an upfront fee.",
                                null,
                                null)),
                        "UNVERIFIED",
                        List.of("Do not pay the fee."),
                        List.of()),
                Instant.parse("2026-01-02T00:00:00Z"));
        analysis.setId("analysis-1");
        when(repository.countByUserId("authenticated-user")).thenReturn(1L);
        when(repository.findTop5ByUserIdOrderByAnalysisTimestampDesc("authenticated-user"))
                .thenReturn(List.of(analysis));
        when(urlScanRepository.countByUserId("authenticated-user")).thenReturn(0L);
        when(urlScanRepository.findTop5ByUserIdOrderByScanTimestampDesc("authenticated-user")).thenReturn(List.of());
        when(savedJobRepository.countByUserId("authenticated-user")).thenReturn(0L);
        when(savedJobRepository.findTop5ByUserIdOrderBySavedAtDesc("authenticated-user")).thenReturn(List.of());

        DashboardResponse response = dashboardService.getOverview(new UserPrincipal(authenticatedUser));

        assertEquals(1, response.stats().totalScamAnalyses());
        assertEquals(1, response.recentActivities().size());
        assertEquals("SCAM_ANALYSIS", response.recentActivities().get(0).type());
        assertEquals("analysis-1", response.recentActivities().get(0).id());
        verify(repository).countByUserId("authenticated-user");
        verify(repository).findTop5ByUserIdOrderByAnalysisTimestampDesc("authenticated-user");
        verify(urlScanRepository).countByUserId("authenticated-user");
        verify(urlScanRepository).findTop5ByUserIdOrderByScanTimestampDesc("authenticated-user");
    }

    @Test
    void dashboardCountsAndIncludesAuthenticatedUsersRecentUrlScans() {
        User authenticatedUser = new User(
                "Morgan Lee",
                "morgan@example.com",
                "password-hash",
                UserRole.USER,
                Instant.parse("2026-01-01T00:00:00Z"));
        authenticatedUser.setId("authenticated-user");
        URLScan scan = new URLScan(
                "authenticated-user",
                "https://example.com/jobs",
                "example.com",
                "https",
                "DOMAIN",
                URLRiskStatus.UNVERIFIED,
                0,
                List.of(),
                new com.scamshield.urlscanner.URLScanResponse.ReputationFinding(
                        "Google Safe Browsing", "NOT_CONFIGURED", null, List.of(), "Not configured."),
                new com.scamshield.urlscanner.URLScanResponse.RedirectSummary(
                        "NOT_CHECKED", null, "Not followed."),
                List.of("Verify before sharing information."),
                Instant.parse("2026-01-03T00:00:00Z"));
        scan.setId("url-scan-1");

        when(repository.countByUserId("authenticated-user")).thenReturn(0L);
        when(repository.findTop5ByUserIdOrderByAnalysisTimestampDesc("authenticated-user")).thenReturn(List.of());
        when(urlScanRepository.countByUserId("authenticated-user")).thenReturn(1L);
        when(urlScanRepository.findTop5ByUserIdOrderByScanTimestampDesc("authenticated-user"))
                .thenReturn(List.of(scan));
        when(savedJobRepository.countByUserId("authenticated-user")).thenReturn(0L);
        when(savedJobRepository.findTop5ByUserIdOrderBySavedAtDesc("authenticated-user")).thenReturn(List.of());

        DashboardResponse response = dashboardService.getOverview(new UserPrincipal(authenticatedUser));

        assertEquals(1, response.stats().urlsScanned());
        assertEquals(1, response.recentActivities().size());
        assertEquals("URL_SCAN", response.recentActivities().get(0).type());
        assertEquals("url-scan-1", response.recentActivities().get(0).id());
        verify(urlScanRepository).countByUserId("authenticated-user");
        verify(urlScanRepository).findTop5ByUserIdOrderByScanTimestampDesc("authenticated-user");
    }

    @Test
    void dashboardCountsAndShowsAuthenticatedUsersResumeAnalyses() {
        User authenticatedUser = new User(
                "Morgan Lee",
                "morgan@example.com",
                "password-hash",
                UserRole.USER,
                Instant.parse("2026-01-01T00:00:00Z"));
        authenticatedUser.setId("authenticated-user");
        var data = new ExtractedResumeData(
                new ExtractedResumeData.PersonalInfo("", "", "", "", "", ""),
                List.of(), List.of("Java"), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of());
        ResumeAnalysis analysis = new ResumeAnalysis(
                "resume-1",
                "authenticated-user",
                data,
                new SkillAnalysisResponse(List.of("Java"), List.of(), List.of()),
                new ResumeScoreResponse(true, 70, "Advisory", List.of(), "Not official"),
                Instant.parse("2026-01-04T00:00:00Z"),
                List.of());
        analysis.setId("resume-analysis-1");
        when(resumeAnalysisRepository.countByOwnerId("authenticated-user")).thenReturn(1L);
        when(resumeAnalysisRepository.findTop5ByOwnerIdOrderByAnalysisTimestampDesc("authenticated-user"))
                .thenReturn(List.of(analysis));

        DashboardResponse response = dashboardService.getOverview(new UserPrincipal(authenticatedUser));

        assertEquals(1, response.stats().resumeAnalyses());
        assertEquals("RESUME_ANALYSIS", response.recentActivities().get(0).type());
        assertEquals("resume-analysis-1", response.recentActivities().get(0).id());
        verify(resumeAnalysisRepository).countByOwnerId("authenticated-user");
    }
}
