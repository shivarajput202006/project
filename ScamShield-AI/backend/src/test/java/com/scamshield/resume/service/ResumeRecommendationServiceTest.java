package com.scamshield.resume.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.scamshield.jobfinder.provider.JobProvider;
import com.scamshield.jobfinder.service.JobProviderService;

class ResumeRecommendationServiceTest {
    @Test
    void returnsRoleSuggestionsWithoutInventingLiveListingsWhenProviderIsUnavailable() {
        JobProvider provider = mock(JobProvider.class);
        when(provider.isConfigured()).thenReturn(false);
        JobProviderService providerService = new JobProviderService(List.of(provider));
        var service = new ResumeRecommendationService(providerService);
        var data = new com.scamshield.resume.dto.ExtractedResumeData(
                new com.scamshield.resume.dto.ExtractedResumeData.PersonalInfo("", "", "", "", "", ""),
                List.of(), List.of("Java"), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(),
                List.of(new com.scamshield.resume.dto.ExtractedResumeData.RecommendedRole(
                        "Java Developer", "Java is listed in the resume.", List.of("Java"), List.of(), "Entry")));
        var analysis = new com.scamshield.resume.model.ResumeAnalysis(
                "resume-1",
                "user-1",
                data,
                new com.scamshield.resume.dto.SkillAnalysisResponse(List.of("Java"), List.of(), List.of()),
                new com.scamshield.resume.dto.ResumeScoreResponse(false, null, "Unavailable", List.of(), "Advisory"),
                java.time.Instant.now(),
                List.of());

        var result = service.recommend(analysis);

        assertTrue(result.roles().stream().anyMatch(role -> role.title().equals("Java Developer")));
        assertFalse(result.liveSearchConfigured());
        assertFalse(result.liveListingsIncluded());
        assertTrue(result.message().contains("Live listings are unavailable"));
    }
}
