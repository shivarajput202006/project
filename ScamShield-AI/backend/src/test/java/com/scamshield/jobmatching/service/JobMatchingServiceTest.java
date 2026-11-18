package com.scamshield.jobmatching.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import com.scamshield.jobfinder.dto.JobSearchRequest;
import com.scamshield.jobfinder.model.Job;
import com.scamshield.jobfinder.model.JobExperience;
import com.scamshield.jobfinder.model.JobType;
import com.scamshield.jobfinder.model.JobVerificationStatus;
import com.scamshield.jobfinder.model.JobWorkMode;
import com.scamshield.jobfinder.provider.ConfiguredJobProvider.JobProviderException;
import com.scamshield.jobfinder.provider.JobProviderSearchResult;
import com.scamshield.jobfinder.service.JobFinderService;
import com.scamshield.jobmatching.dto.JobMatchRequest;
import com.scamshield.jobmatching.model.JobMatch;
import com.scamshield.jobmatching.repository.JobMatchRepository;
import com.scamshield.resume.dto.ExtractedResumeData;
import com.scamshield.resume.model.Resume;
import com.scamshield.resume.model.ResumeAnalysis;
import com.scamshield.resume.model.ResumeFileType;
import com.scamshield.resume.repository.ResumeAnalysisRepository;
import com.scamshield.resume.repository.ResumeRepository;
import com.scamshield.security.UserPrincipal;
import com.scamshield.user.User;
import com.scamshield.user.UserRole;

class JobMatchingServiceTest {
    private final JobFinderService jobFinder = org.mockito.Mockito.mock(JobFinderService.class);
    private final ResumeRepository resumes = org.mockito.Mockito.mock(ResumeRepository.class);
    private final ResumeAnalysisRepository analyses = org.mockito.Mockito.mock(ResumeAnalysisRepository.class);
    private final JobMatchRepository matches = org.mockito.Mockito.mock(JobMatchRepository.class);
    private final JobMatchingService service = new JobMatchingService(
            jobFinder,
            resumes,
            analyses,
            matches,
            new SkillMatchingService(),
            new ExperienceMatchingService(),
            new EducationMatchingService(),
            new MatchScoreService(),
            new SkillRoadmapService());

    @BeforeEach
    void prepareOwnerResume() {
        when(resumes.findByIdAndOwnerId("resume-1", "owner-1"))
                .thenReturn(Optional.of(new Resume("owner-1", "resume.pdf", ResumeFileType.PDF, "storage", Instant.now())));
        when(analyses.findFirstByResumeIdAndOwnerIdOrderByAnalysisTimestampDesc("resume-1", "owner-1"))
                .thenReturn(Optional.of(analysis()));
        when(jobFinder.providerSourceName()).thenReturn("Adzuna");
        when(matches.findByOwnerIdAndResumeIdAndSourceNameAndSourceJobId(
                "owner-1", "resume-1", "Adzuna", "job-1")).thenReturn(Optional.empty());
        when(matches.save(any(JobMatch.class))).thenAnswer(invocation -> {
            JobMatch match = invocation.getArgument(0);
            match.setId("match-1");
            return match;
        });
    }

    @Test
    void combinesOwnedResumeAnalysisWithOnlyLiveProviderListings() {
        Job job = job();
        when(jobFinder.searchForMatching(any(), any(JobSearchRequest.class)))
                .thenReturn(new JobProviderSearchResult(List.of(job), 1, false));

        var result = service.analyze(principal("owner-1"), request());

        assertTrue(result.available());
        assertEquals(1, result.matches().size());
        assertEquals("Developer", result.matches().get(0).job().title());
        assertEquals(List.of("Java"), result.matches().get(0).skills().matchedListingSkills());
        assertEquals(List.of("React"), result.matches().get(0).skills().missingListingSkills());
        assertEquals(75, result.matches().get(0).score());
        verify(matches).save(any(JobMatch.class));
    }

    @Test
    void returnsHonestEmptyResultsWhenProviderHasNoCurrentJobs() {
        when(jobFinder.searchForMatching(any(), any(JobSearchRequest.class)))
                .thenReturn(new JobProviderSearchResult(List.of(), 0, false));

        var result = service.analyze(principal("owner-1"), request());

        assertTrue(result.available());
        assertTrue(result.matches().isEmpty());
        assertTrue(result.message().contains("No current live provider listings"));
        verify(matches, never()).save(any(JobMatch.class));
    }

    @Test
    void propagatesProviderUnavailableWithoutSubstitutingSampleListings() {
        when(jobFinder.searchForMatching(any(), any(JobSearchRequest.class)))
                .thenThrow(new JobProviderException(HttpStatusCode.valueOf(503), "Provider unavailable."));

        var exception = assertThrows(
                JobProviderException.class, () -> service.analyze(principal("owner-1"), request()));

        assertEquals(503, exception.getStatus().value());
        verify(matches, never()).save(any(JobMatch.class));
    }

    @Test
    void rejectsAnotherUsersResumeBeforeCallingLiveProvider() {
        when(resumes.findByIdAndOwnerId("resume-1", "owner-2")).thenReturn(Optional.empty());

        var exception = assertThrows(
                JobMatchingException.class, () -> service.analyze(principal("owner-2"), request()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(jobFinder, never()).searchForMatching(any(), any(JobSearchRequest.class));
    }

    @Test
    void recommendationGetDoesNotPersistAReadOnlyListingResponse() {
        when(jobFinder.searchForMatching(any(), any(JobSearchRequest.class)))
                .thenReturn(new JobProviderSearchResult(List.of(job()), 1, false));

        var result = service.recommendations(
                principal("owner-1"), "resume-1", null, "London", JobWorkMode.REMOTE, JobType.FULL_TIME, 1, 20);

        assertEquals(1, result.matches().size());
        assertEquals(null, result.matches().get(0).id());
        verify(matches, never()).save(any(JobMatch.class));
    }

    @Test
    void refreshUpdatesTheExistingOwnerResumeAndJobMatchInsteadOfDuplicating() {
        JobMatch existing = new JobMatch(
                "owner-1", "resume-1", "Adzuna", "job-1", "Old query", null, null, null,
                20, List.of(), List.of(), List.of(),
                new com.scamshield.jobmatching.dto.MatchExplanationResponse("Old", "Old", "old", List.of()),
                "old", Instant.parse("2025-01-01T00:00:00Z"));
        existing.setId("existing-match");
        when(matches.findByOwnerIdAndResumeIdAndSourceNameAndSourceJobId(
                "owner-1", "resume-1", "Adzuna", "job-1")).thenReturn(Optional.of(existing));
        when(jobFinder.searchForMatching(any(), any(JobSearchRequest.class)))
                .thenReturn(new JobProviderSearchResult(List.of(job()), 1, false));

        service.analyze(principal("owner-1"), request());

        assertEquals("London", existing.getSearchLocation());
        assertEquals(new MatchScoreService().modelVersion(), existing.getScoringModelVersion());
        verify(matches, times(1)).save(existing);
    }

    @Test
    void cannotDeleteAnotherUsersMatchHistory() {
        when(matches.findByIdAndOwnerId("match-foreign", "owner-1")).thenReturn(Optional.empty());

        assertThrows(JobMatchingException.class, () -> service.deleteHistory(principal("owner-1"), "match-foreign"));

        verify(matches, never()).delete(any(JobMatch.class));
    }

    private JobMatchRequest request() {
        return new JobMatchRequest("resume-1", null, "London", JobWorkMode.REMOTE, JobType.FULL_TIME, 1, 20);
    }

    private ResumeAnalysis analysis() {
        ExtractedResumeData data = new ExtractedResumeData(
                null,
                List.of(new ExtractedResumeData.Education("Bachelor", "University", "Computing", "", "", "")),
                List.of("Java"),
                List.of(),
                List.of(new ExtractedResumeData.Experience("WORK", "Developer", "Example", "", "2022-01", "2023-12", "")),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(new ExtractedResumeData.RecommendedRole("Developer", "Resume skills", List.of("Java"), List.of(), "Entry")));
        return new ResumeAnalysis("resume-1", "owner-1", data, null, null, Instant.now(), List.of());
    }

    private Job job() {
        return new Job(
                "provider-id", "Adzuna", "job-1", "Developer", "Example Ltd", "London",
                JobWorkMode.REMOTE, JobExperience.ONE_TO_THREE_YEARS, JobType.FULL_TIME,
                List.of("Java", "React"), "Bachelor's degree required", null, null, null, false,
                Instant.now(), Instant.now(), Instant.now(), JobVerificationStatus.SOURCE_LISTED,
                "https://provider.example/jobs/1", "https://provider.example/jobs/1");
    }

    private UserPrincipal principal(String id) {
        User user = new User("Test User", id + "@example.test", "password-hash", UserRole.USER, Instant.now());
        user.setId(id);
        return new UserPrincipal(user);
    }
}
