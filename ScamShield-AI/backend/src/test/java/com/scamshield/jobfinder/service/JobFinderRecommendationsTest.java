package com.scamshield.jobfinder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.scamshield.jobfinder.model.Job;
import com.scamshield.jobfinder.model.JobVerificationStatus;
import com.scamshield.jobfinder.provider.JobProviderSearchResult;
import com.scamshield.resume.dto.ExtractedResumeData;
import com.scamshield.resume.dto.ResumeScoreResponse;
import com.scamshield.resume.dto.SkillAnalysisResponse;
import com.scamshield.resume.model.Resume;
import com.scamshield.resume.model.ResumeAnalysis;
import com.scamshield.resume.repository.ResumeAnalysisRepository;
import com.scamshield.resume.repository.ResumeRepository;
import com.scamshield.security.UserPrincipal;
import com.scamshield.user.User;
import com.scamshield.user.UserRole;

class JobFinderRecommendationsTest {
    private final JobProviderService providerService = mock(JobProviderService.class);
    private final SavedJobService savedJobService = mock(SavedJobService.class);
    private final JobSearchRateLimiter rateLimiter = mock(JobSearchRateLimiter.class);
    private final ResumeAnalysisRepository analysisRepository = mock(ResumeAnalysisRepository.class);
    private final JobFinderService service = new JobFinderService(
            providerService,
            savedJobService,
            new JobMatchingService(),
            rateLimiter,
            analysisRepository);

    @Test
    void reportsUnavailableWhenResumeHasNotBeenAnalyzed() {
        when(analysisRepository.findFirstByOwnerIdOrderByAnalysisTimestampDesc("user-1"))
                .thenReturn(java.util.Optional.empty());

        var result = service.recommendations(principal());

        assertFalse(result.available());
        assertTrue(result.jobs().isEmpty());
        assertTrue(result.message().contains("No resume data was assumed"));
    }

    @Test
    void returnsOnlyProviderListingsOrderedByResumeSkillOverlap() {
        when(analysisRepository.findFirstByOwnerIdOrderByAnalysisTimestampDesc("user-1"))
                .thenReturn(java.util.Optional.of(analysis()));
        when(providerService.isConfigured()).thenReturn(true);
        Job strongest = job("best", List.of("Java", "Spring"));
        Job weaker = job("other", List.of("Python"));
        when(providerService.search(any()))
                .thenReturn(new JobProviderSearchResult(List.of(weaker, strongest), 2, false));

        var result = service.recommendations(principal());

        assertTrue(result.available());
        assertEquals(List.of("best", "other"), result.jobs().stream().map(item -> item.sourceJobId()).toList());
        assertTrue(result.message().contains("not a qualification"));
        verify(providerService).search(any());
    }

    @Test
    void reportsLiveListingsUnavailableWhenProviderIsNotConfigured() {
        when(analysisRepository.findFirstByOwnerIdOrderByAnalysisTimestampDesc("user-1"))
                .thenReturn(java.util.Optional.of(analysis()));
        when(providerService.isConfigured()).thenReturn(false);

        var result = service.recommendations(principal());

        assertFalse(result.available());
        assertTrue(result.jobs().isEmpty());
        assertTrue(result.message().contains("live listings are unavailable"));
    }

    private ResumeAnalysis analysis() {
        ExtractedResumeData data = new ExtractedResumeData(
                new ExtractedResumeData.PersonalInfo("", "", "", "", "", ""),
                List.of(),
                List.of("Java", "Spring"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(new ExtractedResumeData.RecommendedRole(
                        "Java Developer", "Java is mentioned in the resume.", List.of("Java"), List.of(), "Entry")));
        return new ResumeAnalysis(
                "resume-1", "user-1", data,
                new SkillAnalysisResponse(List.of("Java", "Spring"), List.of(), List.of()),
                new ResumeScoreResponse(true, 70, "Advisory", List.of(), "Not official"),
                Instant.now(), List.of());
    }

    private Job job(String id, List<String> skills) {
        return new Job(
                id,
                "Adzuna",
                id,
                id,
                "Example",
                "Remote",
                null,
                null,
                null,
                skills,
                "",
                null,
                null,
                null,
                false,
                null,
                Instant.now(),
                Instant.now(),
                JobVerificationStatus.SOURCE_LISTED,
                "https://www.adzuna.com/jobs/" + id,
                null);
    }

    private UserPrincipal principal() {
        User user = new User("Test User", "jobs@example.test", "hash", UserRole.USER, Instant.now());
        user.setId("user-1");
        return new UserPrincipal(user);
    }
}
