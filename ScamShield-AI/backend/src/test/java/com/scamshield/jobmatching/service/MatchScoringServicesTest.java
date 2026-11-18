package com.scamshield.jobmatching.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.scamshield.jobfinder.dto.JobSearchRequest;
import com.scamshield.jobfinder.model.Job;
import com.scamshield.jobfinder.model.JobExperience;
import com.scamshield.jobfinder.model.JobType;
import com.scamshield.jobfinder.model.JobVerificationStatus;
import com.scamshield.jobfinder.model.JobWorkMode;
import com.scamshield.jobmatching.dto.EducationComparisonResponse;
import com.scamshield.jobmatching.dto.ExperienceComparisonResponse;
import com.scamshield.jobmatching.dto.JobMatchRequest;
import com.scamshield.jobmatching.dto.SkillComparisonResponse;
import com.scamshield.resume.dto.ExtractedResumeData;

class MatchScoringServicesTest {
    private final ExperienceMatchingService experienceService = new ExperienceMatchingService();
    private final EducationMatchingService educationService = new EducationMatchingService();
    private final MatchScoreService scoreService = new MatchScoreService();

    @Test
    void scoresDocumentedExperienceAndExplicitDegreeRequirement() {
        ExtractedResumeData resume = new ExtractedResumeData(
                null,
                List.of(new ExtractedResumeData.Education("Bachelor of Science", "Example University", "CS", "", "", "")),
                List.of("Java"),
                List.of(),
                List.of(new ExtractedResumeData.Experience(
                        "WORK", "Developer", "Example", "", "2022-01", "2024-12", "")),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        Job job = job(
                List.of("Java", "React"),
                JobExperience.ONE_TO_THREE_YEARS,
                JobWorkMode.REMOTE,
                JobType.FULL_TIME,
                "Bachelor's degree required");

        var experience = experienceService.compare(resume, job);
        var education = educationService.compare(resume, job);
        var skills = new SkillMatchingService().compare(resume, job);
        var result = scoreService.score(
                new JobMatchRequest("resume-1", null, "London", JobWorkMode.REMOTE, JobType.FULL_TIME, 1, 20),
                job,
                skills,
                experience,
                education);

        assertEquals(3, experience.documentedYears());
        assertEquals(100, experience.score());
        assertEquals(100, education.score());
        assertEquals(75, result.score());
        assertEquals(100, result.criteria().stream().mapToInt(c -> c.effectiveWeight()).sum());
    }

    @Test
    void excludesUnknownCriteriaAndMakesScoreUnavailableWhenNoEvidenceCanBeCompared() {
        Job job = job(List.of(), null, null, null, "");
        var result = scoreService.score(
                new JobMatchRequest("resume-1", null, null, null, null, 1, 20),
                job,
                SkillComparisonResponse.unavailable(List.of(), "No listing skills."),
                new ExperienceComparisonResponse(false, null, null, null, "No dates."),
                new EducationComparisonResponse(false, null, null, null, "No stated education."));

        assertNull(result.score());
        assertEquals(0, result.criteria().stream().mapToInt(c -> c.effectiveWeight()).sum());
    }

    @Test
    void doesNotAssumeUndatedExperienceIsZeroYears() {
        ExtractedResumeData resume = new ExtractedResumeData(
                null, List.of(), List.of(), List.of(),
                List.of(new ExtractedResumeData.Experience("WORK", "Developer", "", "", "", "", "")),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        var result = experienceService.compare(
                resume,
                job(List.of(), JobExperience.ONE_TO_THREE_YEARS, null, null, ""));

        assertNull(result.score());
        assertNull(result.documentedYears());
    }

    @Test
    void normalizesApplicableWeightsToExactlyOneHundredAndRejectsInvalidConfiguration() {
        Job job = job(List.of("Java", "React"), JobExperience.ONE_TO_THREE_YEARS, null, null, "");
        var result = scoreService.score(
                new JobMatchRequest("resume-1", null, null, null, null, 1, 20),
                job,
                new SkillComparisonResponse(
                        List.of("Java"), List.of("Java"), List.of("React"), List.of(), List.of(),
                        List.of(), List.of(), List.of(), "Provider terms are not confirmed requirements."),
                new ExperienceComparisonResponse(true, 2, JobExperience.ONE_TO_THREE_YEARS, 100, "Dated"),
                new EducationComparisonResponse(false, null, null, null, "Unknown"));

        assertEquals(100, result.criteria().stream().mapToInt(c -> c.effectiveWeight()).sum());
        assertThrows(IllegalArgumentException.class, () -> new MatchScoreService(50, 20, 10, 10, 11));
    }

    private Job job(
            List<String> skills, JobExperience experience, JobWorkMode mode, JobType type, String description) {
        return new Job(
                "provider-1", "Adzuna", "1", "Developer", "Example", "London",
                mode, experience, type, skills, description, null, null, null, false,
                null, null, null, JobVerificationStatus.SOURCE_LISTED,
                "https://provider.example/job/1", "https://provider.example/job/1");
    }
}
