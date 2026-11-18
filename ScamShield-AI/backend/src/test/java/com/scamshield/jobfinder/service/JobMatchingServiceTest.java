package com.scamshield.jobfinder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.scamshield.jobfinder.model.Job;
import com.scamshield.jobfinder.model.JobVerificationStatus;
import com.scamshield.jobfinder.dto.JobMatchResponse;

class JobMatchingServiceTest {
    private final JobMatchingService service = new JobMatchingService();

    @Test
    void calculatesSkillOverlapAndExplainsTheScoreWithoutClaimingQualification() {
        Job job = job(List.of("Java", "React", "SQL"));

        JobMatchResponse response = service.match(Set.of("java", "sql", "python"), job);

        assertEquals(67, response.score());
        assertEquals(List.of("java", "sql"), response.matchingSkills());
        assertEquals(List.of("react"), response.missingSkills());
        assertTrue(response.explanation().contains("not a qualification"));
    }

    @Test
    void reportsMatchingUnavailableWhenNoResumeSkillsExist() {
        JobMatchResponse response = service.unavailable();

        assertFalse(response.available());
        assertEquals(0, response.score());
        assertTrue(response.explanation().contains("No resume skills were assumed"));
    }

    private Job job(List<String> skills) {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return new Job(
                "adzuna-1", "Adzuna", "1", "Engineer", "Example Co", "London",
                null, null, null, skills, "", null, null, null, false, null, now, now,
                JobVerificationStatus.SOURCE_LISTED, "https://adzuna.co.uk/jobs/land/ad/1", null);
    }
}
