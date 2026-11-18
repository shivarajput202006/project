package com.scamshield.reports.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.scamshield.jobmatching.model.JobMatch;
import com.scamshield.jobmatching.repository.JobMatchRepository;
import com.scamshield.reports.dto.ReportResponse;
import com.scamshield.reports.dto.ReportSectionResponse;
import com.scamshield.reports.dto.ReportSummaryResponse;
import com.scamshield.reports.repository.ReportRepository;
import com.scamshield.reports.enums.ReportStatus;
import com.scamshield.reports.enums.ReportType;
import com.scamshield.resume.model.ResumeAnalysis;
import com.scamshield.resume.repository.ResumeAnalysisRepository;
import com.scamshield.scamdetector.model.ScamAnalysis;
import com.scamshield.scamdetector.repository.ScamAnalysisRepository;
import com.scamshield.urlscanner.URLScan;
import com.scamshield.urlscanner.URLScanRepository;

@Service
public class ReportAggregationService {
    private final ScamAnalysisRepository scamRepository;
    private final URLScanRepository urlRepository;
    private final ResumeAnalysisRepository resumeRepository;
    private final JobMatchRepository jobMatchRepository;
    private final ReportRepository reportRepository;

    public ReportAggregationService(
            ScamAnalysisRepository scamRepository,
            URLScanRepository urlRepository,
            ResumeAnalysisRepository resumeRepository,
            JobMatchRepository jobMatchRepository,
            ReportRepository reportRepository) {
        this.scamRepository = scamRepository;
        this.urlRepository = urlRepository;
        this.resumeRepository = resumeRepository;
        this.jobMatchRepository = jobMatchRepository;
        this.reportRepository = reportRepository;
    }

    public List<ReportResponse> aggregate(String ownerId) {
        List<ReportResponse> reports = new ArrayList<>();
        scamRepository.findByUserIdOrderByAnalysisTimestampDesc(ownerId).stream()
                .map(this::fromScamAnalysis)
                .forEach(reports::add);
        urlRepository.findByUserIdOrderByScanTimestampDesc(ownerId).stream()
                .map(this::fromUrlScan)
                .forEach(reports::add);
        resumeRepository.findByOwnerIdOrderByAnalysisTimestampDesc(ownerId).stream()
                .map(this::fromResumeAnalysis)
                .forEach(reports::add);
        jobMatchRepository.findByOwnerIdOrderByAnalyzedAtDesc(ownerId).stream()
                .map(this::fromJobMatch)
                .forEach(reports::add);
        return List.copyOf(reports);
    }

    public java.util.Optional<ReportResponse> find(String ownerId, String reportId) {
        int separator = reportId == null ? -1 : reportId.indexOf(':');
        if (separator <= 0 || separator == reportId.length() - 1) return java.util.Optional.empty();
        ReportType type;
        try {
            type = ReportType.valueOf(reportId.substring(0, separator));
        } catch (IllegalArgumentException exception) {
            return java.util.Optional.empty();
        }
        String sourceId = reportId.substring(separator + 1);
        return switch (type) {
            case SCAM_DETECTION -> scamRepository.findByIdAndUserId(sourceId, ownerId).map(this::fromScamAnalysis);
            case URL_SCAN -> urlRepository.findByIdAndUserId(sourceId, ownerId).map(this::fromUrlScan);
            case RESUME_ANALYSIS -> resumeRepository.findByIdAndOwnerId(sourceId, ownerId).map(this::fromResumeAnalysis);
            case JOB_MATCHING -> jobMatchRepository.findByIdAndOwnerId(sourceId, ownerId).map(this::fromJobMatch);
        };
    }

    public ReportSummaryResponse summary(String ownerId) {
        long scams = visibleCount(ownerId, ReportType.SCAM_DETECTION, scamRepository.countByUserId(ownerId));
        long urls = visibleCount(ownerId, ReportType.URL_SCAN, urlRepository.countByUserId(ownerId));
        long resumes = visibleCount(ownerId, ReportType.RESUME_ANALYSIS, resumeRepository.countByOwnerId(ownerId));
        long matches = visibleCount(ownerId, ReportType.JOB_MATCHING, jobMatchRepository.countByOwnerId(ownerId));
        Instant lastActivity = java.util.stream.Stream.of(
                        scamRepository.findTop1ByUserIdOrderByAnalysisTimestampDesc(ownerId)
                                .map(ScamAnalysis::getAnalysisTimestamp),
                        urlRepository.findTop1ByUserIdOrderByScanTimestampDesc(ownerId)
                                .map(URLScan::getScanTimestamp),
                        resumeRepository.findFirstByOwnerIdOrderByAnalysisTimestampDesc(ownerId)
                                .map(ResumeAnalysis::getAnalysisTimestamp),
                        jobMatchRepository.findFirstByOwnerIdOrderByAnalyzedAtDesc(ownerId)
                                .map(JobMatch::getAnalyzedAt))
                .flatMap(java.util.Optional::stream)
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(null);
        return new ReportSummaryResponse(scams + urls + resumes + matches, scams, urls, resumes, matches, lastActivity);
    }

    private long visibleCount(String ownerId, ReportType type, long sourceCount) {
        long hiddenCount = reportRepository.countByOwnerIdAndTypeAndHiddenAtIsNotNull(ownerId, type);
        return Math.max(0, sourceCount - hiddenCount);
    }

    private ReportResponse fromScamAnalysis(ScamAnalysis analysis) {
        List<ReportSectionResponse> sections = new ArrayList<>();
        sections.add(section("Input", java.util.Arrays.asList(
                text(analysis.getInputType()), analysis.getInputSummary())));
        sections.add(section("Analysis result", List.of(
                "Risk status: " + text(analysis.getRiskStatus()),
                "Company verification: " + text(analysis.getCompanyVerificationStatus()),
                "Risk score: " + analysis.getRiskScore() + " out of 100")));
        sections.add(section("Risk indicators", analysis.getIndicators() == null ? List.of()
                : analysis.getIndicators().stream()
                        .map(indicator -> join(" — ", indicator.title(), indicator.details()))
                        .toList()));
        sections.add(section("Evidence", analysis.getEvidence() == null ? List.of()
                : analysis.getEvidence().stream()
                        .map(evidence -> join(" — ", evidence.description(), evidence.sourceName()))
                        .toList()));
        sections.add(section("Recommendations", analysis.getRecommendations()));
        sections.add(section("Limitations", analysis.getLimitations()));
        return report(
                ReportType.SCAM_DETECTION,
                analysis.getId(),
                "AI Scam Detector",
                "Scam analysis",
                analysis.getAnalysisTimestamp(),
                analysis.getSummary(),
                analysis.getRiskScore(),
                "Risk score",
                sections);
    }

    private ReportResponse fromUrlScan(URLScan scan) {
        List<ReportSectionResponse> sections = new ArrayList<>();
        sections.add(section("Scanned URL", java.util.Arrays.asList(
                scan.getSubmittedUrl(), scan.getNormalizedDomain())));
        sections.add(section("Security indicators", scan.getSecurityIndicators() == null ? List.of()
                : scan.getSecurityIndicators().stream()
                        .map(indicator -> join(" — ", indicator.title(), indicator.description()))
                        .toList()));
        if (scan.getReputationFinding() != null) {
            sections.add(section("Reputation check", List.of(
                    join(" — ", scan.getReputationFinding().status(), scan.getReputationFinding().sourceName(),
                            scan.getReputationFinding().description()),
                    String.join(", ", scan.getReputationFinding().threatTypes()))));
        }
        if (scan.getRedirectSummary() != null) {
            sections.add(section("Redirect analysis", List.of(
                    join(" — ", scan.getRedirectSummary().status(), scan.getRedirectSummary().detail()))));
        }
        sections.add(section("Recommendations", scan.getRecommendations()));
        return report(
                ReportType.URL_SCAN,
                scan.getId(),
                "Smart URL Scanner",
                "URL scan — " + text(scan.getNormalizedDomain()),
                scan.getScanTimestamp(),
                "The submitted URL was assessed as " + text(scan.getRiskStatus()) + ".",
                scan.getRiskScore(),
                "Risk score",
                sections);
    }

    private ReportResponse fromResumeAnalysis(ResumeAnalysis analysis) {
        var data = analysis.getExtractedData();
        List<ReportSectionResponse> sections = new ArrayList<>();
        sections.add(section("Technical skills", data == null ? List.of() : data.technicalSkills()));
        sections.add(section("Soft skills", data == null ? List.of() : data.softSkills()));
        sections.add(section("Strengths", data == null ? List.of() : data.strengths()));
        sections.add(section("Areas to improve", data == null ? List.of() : data.weaknesses()));
        sections.add(section("Certifications", data == null ? List.of() : data.certifications()));
        sections.add(section("Education", data == null || data.education() == null ? List.of()
                : data.education().stream()
                        .map(education -> join(" — ", education.degree(), education.fieldOfStudy(), education.institution()))
                        .toList()));
        sections.add(section("Experience", data == null || data.experience() == null ? List.of()
                : data.experience().stream()
                        .map(experience -> join(" — ", experience.title(), experience.company(), experience.summary()))
                        .toList()));
        sections.add(section("Projects", data == null || data.projects() == null ? List.of()
                : data.projects().stream()
                        .map(project -> join(" — ", project.name(), project.description()))
                        .toList()));
        sections.add(section("ATS feedback", analysis.getAtsFeedback()));
        sections.add(section("Resume score criteria", analysis.getScore() == null ? List.of()
                : analysis.getScore().criteria().stream()
                        .map(criterion -> join(" — ", criterion.name(), criterion.score() + "%",
                                criterion.explanation(), criterion.improvementAction()))
                        .toList()));
        sections.add(section("Improvement suggestions", analysis.getImprovementSuggestions() == null ? List.of()
                : analysis.getImprovementSuggestions().stream()
                        .map(suggestion -> join(" — ", suggestion.category(), suggestion.suggestion(), suggestion.reason()))
                        .toList()));
        sections.add(section("Recommended roles", data == null || data.recommendedRoles() == null ? List.of()
                : data.recommendedRoles().stream()
                        .map(role -> join(" — ", role.title(), role.rationale(), role.experienceLevel()))
                        .toList()));
        sections.add(section("Limitations", analysis.getLimitations()));
        Integer score = analysis.getScore() != null && analysis.getScore().available()
                ? analysis.getScore().overallScore()
                : null;
        String summary = score == null
                ? "Resume analysis completed. A score was not available from the saved analysis."
                : "Resume feedback score: " + score + " out of 100. This is not an official ATS score.";
        return report(
                ReportType.RESUME_ANALYSIS,
                analysis.getId(),
                "AI Resume Analyzer",
                "Resume analysis",
                analysis.getAnalysisTimestamp(),
                summary,
                score,
                "Resume feedback score (not an official ATS score)",
                sections);
    }

    private ReportResponse fromJobMatch(JobMatch match) {
        List<ReportSectionResponse> sections = new ArrayList<>();
        sections.add(section("Listing reference", List.of(
                "Provider: " + text(match.getSourceName()),
                "Provider job reference: " + text(match.getSourceJobId()),
                "Search role: " + text(match.getSearchKeyword()),
                "Search location: " + text(match.getSearchLocation()))));
        sections.add(section("Skills evidenced in resume", match.getMatchingSkills()));
        sections.add(section("Listing skills not evidenced in resume", match.getMissingSkills()));
        sections.add(section("Scoring criteria", match.getScoreBreakdown() == null ? List.of()
                : match.getScoreBreakdown().stream()
                        .map(criterion -> join(" — ", criterion.criterion(),
                                criterion.score() == null ? "Not scored" : criterion.score() + "%",
                                criterion.explanation()))
                        .toList()));
        if (match.getExplanation() != null) {
            sections.add(section("Explanation", List.of(
                    text(match.getExplanation().summary()),
                    text(match.getExplanation().disclaimer()))));
        }
        return report(
                ReportType.JOB_MATCHING,
                match.getId(),
                "Advanced Job Matching",
                "Job match — " + text(match.getSearchKeyword()),
                match.getAnalyzedAt(),
                match.getExplanation() == null
                        ? "Saved relevance estimate for a provider listing. Listing content may not be retained under provider terms."
                        : match.getExplanation().summary(),
                match.getScore(),
                "Relevance estimate (not a hiring probability)",
                sections);
    }

    private ReportResponse report(
            ReportType type,
            String sourceId,
            String sourceModule,
            String title,
            Instant createdAt,
            String summary,
            Integer score,
            String scoreLabel,
            List<ReportSectionResponse> sections) {
        return new ReportResponse(
                type.name() + ":" + sourceId,
                sourceId,
                sourceModule,
                type,
                title,
                ReportStatus.COMPLETED,
                createdAt,
                text(summary),
                score,
                scoreLabel,
                sections);
    }

    private ReportSectionResponse section(String title, List<String> values) {
        List<String> items = values == null ? List.of() : values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
        return new ReportSectionResponse(title, items);
    }

    private String join(String separator, String... values) {
        return java.util.Arrays.stream(values)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(java.util.stream.Collectors.joining(separator));
    }

    private String text(Object value) {
        return value == null ? "Unavailable" : value.toString();
    }
}
