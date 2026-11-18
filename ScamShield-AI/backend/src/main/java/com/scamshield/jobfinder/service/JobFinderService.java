package com.scamshield.jobfinder.service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.scamshield.jobfinder.dto.JobFiltersResponse;
import com.scamshield.jobfinder.dto.JobRecommendationResponse;
import com.scamshield.jobfinder.dto.JobResponse;
import com.scamshield.jobfinder.dto.JobSearchRequest;
import com.scamshield.jobfinder.dto.JobSearchResponse;
import com.scamshield.jobfinder.model.JobExperience;
import com.scamshield.jobfinder.model.JobType;
import com.scamshield.jobfinder.model.JobWorkMode;
import com.scamshield.jobfinder.provider.JobProviderSearchResult;
import com.scamshield.jobfinder.model.Job;
import com.scamshield.resume.model.ResumeAnalysis;
import com.scamshield.resume.repository.ResumeAnalysisRepository;
import com.scamshield.security.UserPrincipal;

import org.springframework.stereotype.Service;

@Service
public class JobFinderService {
    private final JobProviderService providerService;
    private final SavedJobService savedJobService;
    private final JobMatchingService matchingService;
    private final JobSearchRateLimiter rateLimiter;
    private final ResumeAnalysisRepository resumeAnalysisRepository;

    public JobFinderService(
            JobProviderService providerService,
            SavedJobService savedJobService,
            JobMatchingService matchingService,
            JobSearchRateLimiter rateLimiter,
            ResumeAnalysisRepository resumeAnalysisRepository) {
        this.providerService = providerService;
        this.savedJobService = savedJobService;
        this.matchingService = matchingService;
        this.rateLimiter = rateLimiter;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
    }

    public JobSearchResponse search(UserPrincipal principal, JobSearchRequest request) {
        rateLimiter.check(principal.getUser().getId());
        JobProviderSearchResult results = providerService.search(request);
        List<JobResponse> jobs = results.jobs().stream().map(JobResponse::from).toList();
        return new JobSearchResponse(
                jobs,
                request.page(),
                request.size(),
                results.total(),
                results.hasMore(),
                providerService.sourceName(),
                true);
    }

    public JobProviderSearchResult searchForMatching(UserPrincipal principal, JobSearchRequest request) {
        rateLimiter.check(principal.getUser().getId());
        return providerService.search(request);
    }

    public String providerSourceName() {
        return providerService.sourceName();
    }

    public JobResponse details(UserPrincipal principal, String id) {
        return JobResponse.from(savedJobService.getSavedJob(principal, id));
    }

    public JobFiltersResponse filters() {
        boolean available = providerService.isConfigured();
        return new JobFiltersResponse(
                List.of(JobWorkMode.values()),
                List.of(JobExperience.values()),
                List.of(JobType.values()),
                false,
                providerService.sourceName(),
                available,
                available
                        ? "Work mode and experience are identified only when explicitly mentioned in the listing."
                        : "Configure ADZUNA_APP_ID and ADZUNA_APP_KEY on the backend to enable live job search.");
    }

    public JobResponse save(UserPrincipal principal, String id, com.scamshield.jobfinder.dto.JobSaveRequest request) {
        rateLimiter.check(principal.getUser().getId());
        return savedJobService.save(principal, id, request);
    }

    public void unsave(UserPrincipal principal, String id) {
        savedJobService.unsave(principal, id);
    }

    public List<JobResponse> saved(UserPrincipal principal) {
        return savedJobService.getSaved(principal);
    }

    public JobRecommendationResponse recommendations(UserPrincipal principal) {
        String userId = principal == null || principal.getUser() == null
                ? null
                : principal.getUser().getId();
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("Authenticated user has no database identity.");
        }
        ResumeAnalysis latestAnalysis = resumeAnalysisRepository
                .findFirstByOwnerIdOrderByAnalysisTimestampDesc(userId)
                .orElse(null);
        if (latestAnalysis == null || latestAnalysis.getExtractedData() == null) {
            return unavailable("Analyze a resume to receive job recommendations. No resume data was assumed.");
        }
        if (latestAnalysis.getExtractedData().recommendedRoles().isEmpty()) {
            return unavailable("No roles could be recommended from the latest resume analysis.");
        }
        if (!providerService.isConfigured()) {
            return unavailable(
                    "Resume-based role suggestions are available, but live listings are unavailable until the job provider is configured.");
        }

        rateLimiter.check(userId);
        Set<String> resumeSkills = new LinkedHashSet<>();
        resumeSkills.addAll(latestAnalysis.getExtractedData().technicalSkills());
        resumeSkills.addAll(latestAnalysis.getExtractedData().softSkills());
        var jobsByProviderId = new LinkedHashMap<String, Job>();
        latestAnalysis.getExtractedData().recommendedRoles().stream()
                .limit(3)
                .forEach(role -> {
                    JobProviderSearchResult results = providerService.search(new JobSearchRequest(
                            role.title(), null, null, null, null, null, 1, 10));
                    results.jobs().forEach(job -> jobsByProviderId.putIfAbsent(
                            job.getSourceName() + ":" + job.getSourceJobId(), job));
                });
        List<Job> recommendedJobs = jobsByProviderId.values().stream()
                .sorted(java.util.Comparator
                        .comparingInt((Job job) -> matchingService.match(resumeSkills, job).score())
                        .reversed()
                        .thenComparing(Job::getTitle))
                .limit(20)
                .toList();
        if (recommendedJobs.isEmpty()) {
            return unavailable("No current provider listings matched the suggested resume-based roles.");
        }
        return new JobRecommendationResponse(
                true,
                recommendedJobs.stream().map(JobResponse::from).toList(),
                "Current provider listings ordered by overlap with skills explicitly identified in the resume. "
                        + "Skill overlap is not a qualification or hiring prediction.");
    }

    private JobRecommendationResponse unavailable(String message) {
        return new JobRecommendationResponse(false, List.of(), message);
    }
}
