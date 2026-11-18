package com.scamshield.jobmatching.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.scamshield.jobfinder.model.Job;
import com.scamshield.jobfinder.model.JobType;
import com.scamshield.jobfinder.model.JobVerificationStatus;
import com.scamshield.jobfinder.model.JobWorkMode;
import com.scamshield.resume.dto.ExtractedResumeData;

class SkillMatchingServiceTest {
    private final SkillMatchingService service = new SkillMatchingService();

    @Test
    void normalizesAliasesAndDeduplicatesListingSkills() {
        var comparison = service.compare(resume(List.of("JavaScript", "JS")), job(List.of("JS", "JavaScript", "React", "React")));

        assertEquals(List.of("JS"), comparison.matchedListingSkills());
        assertEquals(List.of("React"), comparison.missingListingSkills());
        assertEquals(List.of(), comparison.matchedRequiredSkills());
        assertEquals(List.of(), comparison.missingRequiredSkills());
        assertEquals(List.of(), comparison.matchedPreferredSkills());
        assertEquals(List.of(), comparison.missingPreferredSkills());
    }

    private ExtractedResumeData resume(List<String> skills) {
        return new ExtractedResumeData(
                null, List.of(), skills, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of());
    }

    static Job job(List<String> skills) {
        return new Job(
                "provider-1", "Adzuna", "1", "Developer", "Example", "London",
                JobWorkMode.REMOTE, null, JobType.FULL_TIME, skills, "Bachelor's degree",
                null, null, null, false, null, null, null, JobVerificationStatus.SOURCE_LISTED,
                "https://provider.example/job/1", "https://provider.example/job/1");
    }
}
