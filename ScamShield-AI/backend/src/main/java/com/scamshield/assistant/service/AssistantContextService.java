package com.scamshield.assistant.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.scamshield.assistant.enums.ContextSource;
import com.scamshield.jobfinder.model.Job;
import com.scamshield.jobfinder.repository.JobRepository;
import com.scamshield.jobmatching.model.JobMatch;
import com.scamshield.jobmatching.repository.JobMatchRepository;
import com.scamshield.profile.model.ProfileSkill;
import com.scamshield.profile.repository.UserProfileRepository;
import com.scamshield.resume.model.ResumeAnalysis;
import com.scamshield.resume.repository.ResumeAnalysisRepository;
import com.scamshield.scamdetector.model.ScamAnalysis;
import com.scamshield.scamdetector.repository.ScamAnalysisRepository;
import com.scamshield.urlscanner.URLScan;
import com.scamshield.urlscanner.URLScanRepository;

@Service
public class AssistantContextService {
    private static final int MAX_CONTEXT_CHARS = 6000;

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final JobMatchRepository jobMatchRepository;
    private final JobRepository jobRepository;
    private final ScamAnalysisRepository scamAnalysisRepository;
    private final URLScanRepository urlScanRepository;
    private final UserProfileRepository userProfileRepository;

    public AssistantContextService(
            ResumeAnalysisRepository resumeAnalysisRepository,
            JobMatchRepository jobMatchRepository,
            JobRepository jobRepository,
            ScamAnalysisRepository scamAnalysisRepository,
            URLScanRepository urlScanRepository,
            UserProfileRepository userProfileRepository) {
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.jobMatchRepository = jobMatchRepository;
        this.jobRepository = jobRepository;
        this.scamAnalysisRepository = scamAnalysisRepository;
        this.urlScanRepository = urlScanRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public String buildContext(String userId, List<ContextSource> sources) {
        if (sources == null || sources.isEmpty()) return "";
        List<String> sections = new ArrayList<>();
        for (ContextSource source : sources.stream().distinct().toList()) {
            String section = switch (source) {
                case RESUME_ANALYSIS -> resumeContext(userId);
                case JOB_MATCHING -> jobMatchContext(userId);
                case LIVE_JOBS -> liveJobsContext();
                case SCAM_ANALYSES -> scamContext(userId);
                case URL_SCANS -> urlScanContext(userId);
                case PROFILE -> profileContext(userId);
            };
            sections.add(section);
        }
        String context = String.join("\n\n", sections);
        return context.length() <= MAX_CONTEXT_CHARS
                ? context
                : context.substring(0, MAX_CONTEXT_CHARS) + "\n[Context shortened to fit the conversation limit.]";
    }

    private String resumeContext(String userId) {
        ResumeAnalysis analysis = resumeAnalysisRepository.findFirstByOwnerIdOrderByAnalysisTimestampDesc(userId)
                .orElse(null);
        if (analysis == null || analysis.getExtractedData() == null) {
            return "Resume analysis context: unavailable; no saved analysis exists for this account.";
        }
        var data = analysis.getExtractedData();
        return "Resume analysis context (owner's latest saved analysis, " + analysis.getAnalysisTimestamp() + "): "
                + "technical skills=" + limit(data.technicalSkills(), 30)
                + "; soft skills=" + limit(data.softSkills(), 20)
                + "; strengths=" + limit(data.strengths(), 8)
                + "; improvement suggestions="
                + data.improvementSuggestions().stream().limit(8).map(item -> item.suggestion()).toList()
                + "; recommended roles="
                + data.recommendedRoles().stream().limit(6).map(role -> role.title()).toList()
                + ". These are stored analysis suggestions, not independently verified facts.";
    }

    private String jobMatchContext(String userId) {
        JobMatch match = jobMatchRepository.findFirstByOwnerIdOrderByAnalyzedAtDesc(userId).orElse(null);
        if (match == null) return "Job matching context: unavailable; no saved match exists for this account.";
        return "Job matching context (latest saved analysis, " + match.getAnalyzedAt() + "): score="
                + match.getScore() + "; matching skills=" + limit(match.getMatchingSkills(), 25)
                + "; skills not evidenced=" + limit(match.getMissingSkills(), 25)
                + "; explanation="
                + (match.getExplanation() == null ? "not available" : match.getExplanation().summary())
                + ". A match score is an advisory overlap, not a hiring prediction.";
    }

    private String liveJobsContext() {
        List<Job> recentJobs = jobRepository.findAll(PageRequest.of(
                        0, 10, Sort.by(Sort.Direction.DESC, "lastUpdatedAt")))
                .getContent()
                .stream()
                .filter(job -> job.getFetchedAt() != null
                        && job.getFetchedAt().isAfter(Instant.now().minus(7, ChronoUnit.DAYS)))
                .limit(5)
                .toList();
        if (recentJobs.isEmpty()) {
            return "Live job context: unavailable; no recent provider listings are stored. No listings were invented.";
        }
        return "Live job context (recent provider-sourced listings; not independently verified): "
                + recentJobs.stream()
                        .map(job -> job.getTitle() + " at " + job.getCompany() + " (" + job.getLocation()
                                + ", source " + job.getSourceName() + ", verification "
                                + job.getVerificationStatus() + ", fetched " + job.getFetchedAt() + ")")
                        .toList();
    }

    private String scamContext(String userId) {
        List<ScamAnalysis> analyses = scamAnalysisRepository
                .findTop5ByUserIdOrderByAnalysisTimestampDesc(userId);
        if (analyses.isEmpty()) {
            return "Scam analysis context: unavailable; no saved analyses exist for this account.";
        }
        return "Scam analysis context (latest saved results; submitted content omitted): "
                + analyses.stream().limit(3).map(analysis -> analysis.getRiskStatus() + " advisory score "
                        + analysis.getRiskScore() + ", summary: " + trim(analysis.getSummary(), 260)
                        + ", recorded " + analysis.getAnalysisTimestamp()).toList()
                + ". These are advisory model assessments, not proof of fraud.";
    }

    private String urlScanContext(String userId) {
        List<URLScan> scans = urlScanRepository.findTop5ByUserIdOrderByScanTimestampDesc(userId);
        if (scans.isEmpty()) return "URL scan context: unavailable; no saved scans exist for this account.";
        return "URL scan context (latest saved results): "
                + scans.stream().limit(3).map(scan -> scan.getNormalizedDomain() + " was assessed as "
                        + scan.getRiskStatus() + " with advisory score " + scan.getRiskScore()
                        + " on " + scan.getScanTimestamp()).toList()
                + ". These are scanner assessments, not a guarantee of safety.";
    }

    private String profileContext(String userId) {
        var profile = userProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null) return "Profile context: unavailable; no profile information is saved.";
        List<String> skills = profile.getSkills().stream().map(ProfileSkill::name).limit(30).toList();
        List<String> education = profile.getEducation().stream()
                .map(item -> item.getDegree() + " in " + item.getCourse())
                .limit(8)
                .toList();
        return "User-provided profile context: skills=" + skills + "; education=" + education
                + "; professional bio=" + trim(profile.getProfessionalBio(), 500)
                + ". These details were user-provided and are not independently verified.";
    }

    private List<String> limit(List<String> items, int max) {
        return items == null ? List.of() : items.stream().limit(max).toList();
    }

    private String trim(String value, int max) {
        if (value == null || value.isBlank()) return "not available";
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= max ? normalized : normalized.substring(0, max) + "...";
    }
}
