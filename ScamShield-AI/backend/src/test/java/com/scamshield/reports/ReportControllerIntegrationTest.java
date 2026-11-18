package com.scamshield.reports;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.scamshield.reports.repository.ReportRepository;
import com.scamshield.jobfinder.model.JobType;
import com.scamshield.jobfinder.model.JobWorkMode;
import com.scamshield.jobmatching.dto.MatchCriterionResponse;
import com.scamshield.jobmatching.dto.MatchExplanationResponse;
import com.scamshield.jobmatching.model.JobMatch;
import com.scamshield.jobmatching.repository.JobMatchRepository;
import com.scamshield.resume.dto.ExtractedResumeData;
import com.scamshield.resume.dto.ResumeScoreResponse;
import com.scamshield.resume.dto.SkillAnalysisResponse;
import com.scamshield.resume.model.ResumeAnalysis;
import com.scamshield.resume.repository.ResumeAnalysisRepository;
import com.scamshield.scamdetector.model.AiAnalysisResult;
import com.scamshield.scamdetector.model.InputType;
import com.scamshield.scamdetector.model.RiskStatus;
import com.scamshield.scamdetector.model.ScamAnalysis;
import com.scamshield.scamdetector.repository.ScamAnalysisRepository;
import com.scamshield.security.UserPrincipal;
import com.scamshield.urlscanner.URLRiskStatus;
import com.scamshield.urlscanner.URLScan;
import com.scamshield.urlscanner.URLScanRepository;
import com.scamshield.user.User;
import com.scamshield.user.UserRole;

@SpringBootTest(
        properties = {
            "spring.data.mongodb.uri=mongodb://localhost:27017/scamshield_test",
            "app.jwt.secret-base64=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
            "app.auth.cookie-secure=false",
            "app.cors.allowed-origin=http://localhost:5173",
            "app.jobs.adzuna.app-id=",
            "app.jobs.adzuna.app-key="
        })
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ReportControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ScamAnalysisRepository scamRepository;
    @Autowired private URLScanRepository urlRepository;
    @Autowired private ResumeAnalysisRepository resumeRepository;
    @Autowired private JobMatchRepository jobMatchRepository;
    @Autowired private ReportRepository reportRepository;

    private String ownerId;
    private final java.util.List<String> sourceIds = new java.util.ArrayList<>();

    @AfterEach
    void cleanUp() {
        if (ownerId != null) {
            sourceIds.forEach(id -> {
                scamRepository.deleteById(id);
                urlRepository.deleteById(id);
                resumeRepository.deleteById(id);
                jobMatchRepository.deleteById(id);
            });
            reportRepository.findByOwnerIdAndHiddenAtIsNotNull(ownerId).forEach(reportRepository::delete);
        }
    }

    @Test
    void listsOwnedSourceRecordExportsPdfAndHidesHistoryWithoutDeletingAnalysis() throws Exception {
        ownerId = "reports-" + UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-10-10T12:00:00Z");
        ScamAnalysis source = scamRepository.save(new ScamAnalysis(
                ownerId,
                InputType.TEXT,
                "Sample warning message",
                new AiAnalysisResult(
                        RiskStatus.NEEDS_REVIEW,
                        67,
                        "The message requests an upfront payment.",
                        List.of(),
                        List.of(),
                        "UNVERIFIED",
                        List.of("Verify the employer independently."),
                        List.of("This is an automated assessment.")),
                createdAt));
        sourceIds.add(source.getId());
        URLScan urlScan = urlRepository.save(new URLScan(
                ownerId, "https://example.test", "example.test", "https", "DOMAIN",
                URLRiskStatus.UNVERIFIED, 20, List.of(), null, null,
                List.of("Confirm the destination before signing in."), createdAt));
        sourceIds.add(urlScan.getId());
        ExtractedResumeData resumeData = new ExtractedResumeData(
                null, List.of(), List.of("Java"), List.of(), List.of(), List.of(),
                List.of(), List.of("Clear project descriptions"), List.of(), List.of(),
                List.of(), List.of());
        ResumeAnalysis resumeAnalysis = resumeRepository.save(new ResumeAnalysis(
                "resume-reference",
                ownerId,
                resumeData,
                new SkillAnalysisResponse(List.of("Java"), List.of(), List.of()),
                new ResumeScoreResponse(true, 78, "Developing", List.of(), "Advisory only."),
                createdAt,
                List.of()));
        sourceIds.add(resumeAnalysis.getId());
        JobMatch jobMatch = jobMatchRepository.save(new JobMatch(
                ownerId, "resume-reference", "Adzuna", "provider-job-reference", "Java Developer", "London",
                JobWorkMode.REMOTE, JobType.FULL_TIME, 81,
                List.of(new MatchCriterionResponse("Technical skills", 50, 80, 50, "Java appears in both.")),
                List.of("Java"), List.of("React"),
                new MatchExplanationResponse("Relevant listing.", "Not a hiring probability.", "v1", List.of()),
                "v1", createdAt));
        sourceIds.add(jobMatch.getId());
        UserPrincipal principal = principal(ownerId);
        String reportId = "SCAM_DETECTION:" + source.getId();

        mockMvc.perform(get("/api/reports")
                        .with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.reports[0].id").value(reportId))
                .andExpect(jsonPath("$.reports[0].sections").isEmpty());

        mockMvc.perform(get("/api/reports/summary").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReports").value(4))
                .andExpect(jsonPath("$.scamDetections").value(1))
                .andExpect(jsonPath("$.urlScans").value(1))
                .andExpect(jsonPath("$.resumeAnalyses").value(1))
                .andExpect(jsonPath("$.jobMatches").value(1));

        mockMvc.perform(get("/api/reports/" + reportId).with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("The message requests an upfront payment."));

        List<String> reportIds = List.of(
                reportId,
                "URL_SCAN:" + urlScan.getId(),
                "RESUME_ANALYSIS:" + resumeAnalysis.getId(),
                "JOB_MATCHING:" + jobMatch.getId());
        for (String id : reportIds) {
            mockMvc.perform(get("/api/reports/" + id + "/download").with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                    .andExpect(result -> assertPdf(result));
        }

        mockMvc.perform(get("/api/reports/" + reportId)
                        .with(user(principal("different-owner"))))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/reports/" + reportId)
                        .with(user(principal))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/reports/summary").with(user(principal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReports").value(3));
        mockMvc.perform(get("/api/reports/" + reportId + "/download").with(user(principal)))
                .andExpect(status().isNotFound());
        assertTrue(scamRepository.findByIdAndUserId(source.getId(), ownerId).isPresent());
    }

    private void assertPdf(MvcResult result) throws Exception {
        byte[] bytes = result.getResponse().getContentAsByteArray();
        assertTrue(bytes.length > 100);
        assertTrue(new String(bytes, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).startsWith("%PDF-"));
    }

    private UserPrincipal principal(String id) {
        User user = new User("Reports Test", id + "@example.test", "password-hash", UserRole.USER, Instant.now());
        user.setId(id);
        return new UserPrincipal(user);
    }
}
