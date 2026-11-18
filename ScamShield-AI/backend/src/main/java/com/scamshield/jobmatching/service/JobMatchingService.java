package com.scamshield.jobmatching.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.scamshield.jobfinder.dto.JobResponse;
import com.scamshield.jobfinder.dto.JobSearchRequest;
import com.scamshield.jobfinder.model.Job;
import com.scamshield.jobfinder.provider.JobProviderSearchResult;
import com.scamshield.jobfinder.service.JobFinderService;
import com.scamshield.jobmatching.dto.EducationComparisonResponse;
import com.scamshield.jobmatching.dto.ExperienceComparisonResponse;
import com.scamshield.jobmatching.dto.JobMatchHistoryResponse;
import com.scamshield.jobmatching.dto.JobMatchRequest;
import com.scamshield.jobmatching.dto.JobMatchResponse;
import com.scamshield.jobmatching.dto.JobMatchSearchResponse;
import com.scamshield.jobmatching.dto.MatchCriterionResponse;
import com.scamshield.jobmatching.dto.MatchExplanationResponse;
import com.scamshield.jobmatching.dto.SkillComparisonResponse;
import com.scamshield.jobmatching.dto.SkillRoadmapItem;
import com.scamshield.jobmatching.enums.MatchStatus;
import com.scamshield.jobmatching.model.JobMatch;
import com.scamshield.jobmatching.repository.JobMatchRepository;
import com.scamshield.resume.dto.ExtractedResumeData;
import com.scamshield.resume.model.ResumeAnalysis;
import com.scamshield.resume.repository.ResumeAnalysisRepository;
import com.scamshield.resume.repository.ResumeRepository;
import com.scamshield.security.UserPrincipal;
import com.scamshield.notifications.NotificationEventPublisher;
import com.scamshield.notifications.NotificationType;

@Service("advancedJobMatchingService")
public class JobMatchingService {
    private static final int MAX_ROLE_QUERIES = 3;
    private final JobFinderService jobFinderService;
    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final JobMatchRepository jobMatchRepository;
    private final SkillMatchingService skillMatchingService;
    private final ExperienceMatchingService experienceMatchingService;
    private final EducationMatchingService educationMatchingService;
    private final MatchScoreService matchScoreService;
    private final SkillRoadmapService skillRoadmapService;
    private NotificationEventPublisher notificationEventPublisher;

    @Autowired(required = false)
    public void setNotificationEventPublisher(NotificationEventPublisher publisher) {
        this.notificationEventPublisher = publisher;
    }

    public JobMatchingService(
            JobFinderService jobFinderService,
            ResumeRepository resumeRepository,
            ResumeAnalysisRepository resumeAnalysisRepository,
            JobMatchRepository jobMatchRepository,
            SkillMatchingService skillMatchingService,
            ExperienceMatchingService experienceMatchingService,
            EducationMatchingService educationMatchingService,
            MatchScoreService matchScoreService,
            SkillRoadmapService skillRoadmapService) {
        this.jobFinderService = jobFinderService;
        this.resumeRepository = resumeRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.jobMatchRepository = jobMatchRepository;
        this.skillMatchingService = skillMatchingService;
        this.experienceMatchingService = experienceMatchingService;
        this.educationMatchingService = educationMatchingService;
        this.matchScoreService = matchScoreService;
        this.skillRoadmapService = skillRoadmapService;
    }

    public JobMatchSearchResponse analyze(UserPrincipal principal, JobMatchRequest request) {
        JobMatchSearchResponse response = match(principal, request, true);
        publishMatches(principal, response);
        return response;
    }

    private void publishMatches(UserPrincipal principal, JobMatchSearchResponse response) {
        if (notificationEventPublisher == null || response.matches().isEmpty()) return;
        String ownerId = ownerId(principal);
        String eventKey = "JOB_MATCH:" + response.resumeId() + ":"
                + response.matches().stream().map(match -> match.sourceName() + ":" + match.sourceJobId())
                        .sorted().limit(5).collect(java.util.stream.Collectors.joining("|"));
        notificationEventPublisher.publish(
                ownerId, eventKey, NotificationType.JOB_MATCH,
                "Job matches are ready", "New resume-based job matches are ready to review.",
                "/job-matching");
    }

    private JobMatchSearchResponse match(
            UserPrincipal principal, JobMatchRequest request, boolean persistMatches) {
        String ownerId = ownerId(principal);
        ResumeAnalysis analysis = findAnalysis(request.resumeId(), ownerId);
        List<String> queries = roleQueries(request, analysis.getExtractedData());
        Map<String, Candidate> candidates = new LinkedHashMap<>();
        long total = 0;
        boolean hasMore = false;
        for (String query : queries) {
            JobProviderSearchResult result = jobFinderService.searchForMatching(
                    principal,
                    new JobSearchRequest(
                            query,
                            request.location(),
                            request.workMode(),
                            null,
                            request.jobType(),
                            null,
                            request.page(),
                            request.size()));
            total += result.total();
            hasMore |= result.hasMore();
            for (Job job : result.jobs()) {
                String key = job.getSourceName() + ":" + job.getSourceJobId();
                candidates.putIfAbsent(key, new Candidate(job, query));
            }
        }

        List<Candidate> ranked = candidates.values().stream()
                .map(candidate -> evaluate(ownerId, request, analysis, candidate, persistMatches, null))
                .sorted(Comparator
                        .comparing((Candidate candidate) -> candidate.score().score(),
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(candidate -> candidate.job().getTitle(), String.CASE_INSENSITIVE_ORDER))
                .toList();
        int to = Math.min(request.size(), ranked.size());
        List<JobMatchResponse> matches = ranked.subList(0, to).stream()
                .map(Candidate::response)
                .toList();
        hasMore |= ranked.size() > request.size();

        return new JobMatchSearchResponse(
                true,
                request.resumeId(),
                matches,
                request.page(),
                request.size(),
                Math.max(total, ranked.size()),
                hasMore,
                matches.isEmpty() ? jobFinderService.providerSourceName() : matches.get(0).sourceName(),
                matches.isEmpty()
                        ? "No current live provider listings matched this resume and filter selection."
                        : "Live listings are ranked with an explainable relevance estimate. Missing skills mean not evidenced in this resume, not a confirmed ability gap.",
                matchScoreService.modelVersion());
    }

    public JobMatchSearchResponse recommendations(
            UserPrincipal principal,
            String resumeId,
            String keyword,
            String location,
            com.scamshield.jobfinder.model.JobWorkMode workMode,
            com.scamshield.jobfinder.model.JobType jobType,
            int page,
            int size) {
        String ownerId = ownerId(principal);
        ResumeAnalysis analysis;
        if (resumeId == null || resumeId.isBlank()) {
            analysis = resumeAnalysisRepository.findFirstByOwnerIdOrderByAnalysisTimestampDesc(ownerId).orElse(null);
            if (analysis == null) return unavailable(null, "Analyze a resume before requesting job matches.");
            resumeId = analysis.getResumeId();
        } else {
            resumeRepository.findByIdAndOwnerId(resumeId, ownerId)
                    .orElseThrow(() -> new JobMatchingException(HttpStatus.NOT_FOUND, "The selected resume was not found."));
            analysis = resumeAnalysisRepository
                    .findFirstByResumeIdAndOwnerIdOrderByAnalysisTimestampDesc(resumeId, ownerId)
                    .orElse(null);
            if (analysis == null) return unavailable(resumeId, "The selected resume has not been analyzed yet.");
        }
        return match(
                principal,
                new JobMatchRequest(resumeId, keyword, location, workMode, jobType, page, size),
                false);
    }

    public JobMatchResponse details(UserPrincipal principal, String sourceJobId) {
        String ownerId = ownerId(principal);
        JobMatch saved = jobMatchRepository
                .findFirstByOwnerIdAndSourceJobIdOrderByAnalyzedAtDesc(ownerId, sourceJobId)
                .orElseThrow(JobMatchingException::notFound);
        ResumeAnalysis analysis = findAnalysis(saved.getResumeId(), ownerId);
        JobProviderSearchResult result = jobFinderService.searchForMatching(
                principal,
                new JobSearchRequest(
                        saved.getSearchKeyword(),
                        saved.getSearchLocation(),
                        saved.getPreferredWorkMode(),
                        null,
                        saved.getPreferredJobType(),
                        null,
                        1,
                        50));
        Job job = result.jobs().stream()
                .filter(item -> item.getSourceName().equals(saved.getSourceName())
                        && item.getSourceJobId().equals(saved.getSourceJobId()))
                .findFirst()
                .orElseThrow(() -> new JobMatchingException(
                        HttpStatus.NOT_FOUND, "This listing is no longer available from the live provider."));
        return evaluate(ownerId, new JobMatchRequest(
                        saved.getResumeId(),
                        saved.getSearchKeyword(),
                        saved.getSearchLocation(),
                        saved.getPreferredWorkMode(),
                        saved.getPreferredJobType(),
                        1,
                        50),
                analysis,
                new Candidate(job, saved.getSearchKeyword()),
                false,
                saved.getId())
                .response();
    }

    public List<JobMatchHistoryResponse> history(UserPrincipal principal, String resumeId) {
        String ownerId = ownerId(principal);
        List<JobMatch> matches;
        if (resumeId == null || resumeId.isBlank()) {
            matches = jobMatchRepository.findTop100ByOwnerIdOrderByAnalyzedAtDesc(ownerId);
        } else {
            resumeRepository.findByIdAndOwnerId(resumeId, ownerId)
                    .orElseThrow(() -> new JobMatchingException(HttpStatus.NOT_FOUND, "The selected resume was not found."));
            matches = jobMatchRepository.findTop100ByOwnerIdAndResumeIdOrderByAnalyzedAtDesc(ownerId, resumeId);
        }
        return matches.stream()
                .map(match -> new JobMatchHistoryResponse(
                        match.getId(),
                        match.getResumeId(),
                        match.getSourceName(),
                        match.getSourceJobId(),
                        match.getScore(),
                        match.getScoringModelVersion(),
                        match.getStatus(),
                        match.getAnalyzedAt()))
                .toList();
    }

    public void deleteHistory(UserPrincipal principal, String id) {
        String ownerId = ownerId(principal);
        JobMatch match = jobMatchRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(JobMatchingException::notFound);
        jobMatchRepository.delete(match);
    }

    private Candidate evaluate(
            String ownerId,
            JobMatchRequest request,
            ResumeAnalysis analysis,
            Candidate candidate,
            boolean persistMatch,
            String existingMatchId) {
        ExtractedResumeData resume = analysis.getExtractedData();
        Job job = candidate.job();
        SkillComparisonResponse skills = skillMatchingService.compare(resume, job);
        ExperienceComparisonResponse experience = experienceMatchingService.compare(resume, job);
        EducationComparisonResponse education = educationMatchingService.compare(resume, job);
        MatchScoreService.ScoreResult score = matchScoreService.score(request, job, skills, experience, education);
        String summary = score.score() == null
                ? "There is not enough comparable resume and listing information to calculate a match score."
                : "Estimated " + score.score()
                        + "% relevance using only criteria with comparable evidence; applicable weights are renormalized.";
        MatchExplanationResponse explanation = new MatchExplanationResponse(
                summary,
                "A relevance estimate only. It is not a probability of being hired, a job-safety guarantee, or an ATS result.",
                matchScoreService.modelVersion(),
                score.criteria());
        Instant analyzedAt = Instant.now();
        String matchId = existingMatchId;
        if (persistMatch) {
            JobMatch draft = new JobMatch(
                    ownerId,
                    request.resumeId(),
                    job.getSourceName(),
                    job.getSourceJobId(),
                    candidate.query(),
                    request.location(),
                    request.workMode(),
                    request.jobType(),
                    score.score(),
                    score.criteria(),
                    skills.matchedListingSkills(),
                    skills.missingListingSkills(),
                    explanation,
                    matchScoreService.modelVersion(),
                    analyzedAt);
            JobMatch persisted = jobMatchRepository
                    .findByOwnerIdAndResumeIdAndSourceNameAndSourceJobId(
                            ownerId, request.resumeId(), job.getSourceName(), job.getSourceJobId())
                    .map(existing -> {
                        existing.updateFrom(draft);
                        return jobMatchRepository.save(existing);
                    })
                    .orElseGet(() -> jobMatchRepository.save(draft));
            matchId = persisted.getId();
        }

        List<SkillRoadmapItem> roadmap = skillRoadmapService.build(job, resume, skills);
        String locationCompatibility = score.criteria().stream()
                .filter(item -> item.criterion().equals("Location and work mode"))
                .map(MatchCriterionResponse::explanation)
                .findFirst()
                .orElse("");
        String jobTypeCompatibility = score.criteria().stream()
                .filter(item -> item.criterion().equals("Job type"))
                .map(MatchCriterionResponse::explanation)
                .findFirst()
                .orElse("");
        JobMatchResponse response = new JobMatchResponse(
                matchId,
                request.resumeId(),
                job.getSourceName(),
                job.getSourceJobId(),
                MatchStatus.MATCHED,
                JobResponse.from(job),
                score.score(),
                score.criteria(),
                skills,
                experience,
                education,
                locationCompatibility,
                jobTypeCompatibility,
                explanation,
                roadmap,
                analyzedAt);
        return new Candidate(job, candidate.query(), score, response);
    }

    private List<String> roleQueries(JobMatchRequest request, ExtractedResumeData resume) {
        if (request.keyword() != null && !request.keyword().isBlank()) {
            return List.of(request.keyword().trim());
        }
        if (resume == null || resume.recommendedRoles() == null) {
            throw new JobMatchingException(HttpStatus.UNPROCESSABLE_ENTITY, "The selected resume has no role recommendations to search.");
        }
        List<String> queries = resume.recommendedRoles().stream()
                .map(ExtractedResumeData.RecommendedRole::title)
                .filter(title -> title != null && !title.isBlank())
                .map(String::trim)
                .distinct()
                .limit(MAX_ROLE_QUERIES)
                .toList();
        if (queries.isEmpty()) {
            throw new JobMatchingException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "No job roles were identified in this resume analysis. Provide a keyword to search current listings.");
        }
        return queries;
    }

    private ResumeAnalysis findAnalysis(String resumeId, String ownerId) {
        resumeRepository.findByIdAndOwnerId(resumeId, ownerId)
                .orElseThrow(() -> new JobMatchingException(HttpStatus.NOT_FOUND, "The selected resume was not found."));
        return resumeAnalysisRepository.findFirstByResumeIdAndOwnerIdOrderByAnalysisTimestampDesc(resumeId, ownerId)
                .orElseThrow(() -> new JobMatchingException(
                        HttpStatus.NOT_FOUND, "The selected resume has no completed analysis."));
    }

    private String ownerId(UserPrincipal principal) {
        String id = principal == null || principal.getUser() == null ? null : principal.getUser().getId();
        if (id == null || id.isBlank()) {
            throw new JobMatchingException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return id;
    }

    private JobMatchSearchResponse unavailable(String resumeId, String message) {
        return new JobMatchSearchResponse(
                false,
                resumeId,
                List.of(),
                1,
                20,
                0,
                false,
                jobFinderService.providerSourceName(),
                message,
                matchScoreService.modelVersion());
    }

    private record Candidate(
            Job job,
            String query,
            MatchScoreService.ScoreResult score,
            JobMatchResponse response) {
        Candidate(Job job, String query) {
            this(job, query, null, null);
        }

        Candidate(Job job, String query, MatchScoreService.ScoreResult score, JobMatchResponse response) {
            this.job = job;
            this.query = query;
            this.score = score;
            this.response = response;
        }
    }
}
