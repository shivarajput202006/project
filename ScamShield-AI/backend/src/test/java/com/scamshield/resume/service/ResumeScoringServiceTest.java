package com.scamshield.resume.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.scamshield.resume.dto.ExtractedResumeData;

class ResumeScoringServiceTest {
    private final ResumeScoringService service = new ResumeScoringService();

    @Test
    void calculatesExplainableChecklistScoreFromDocumentEvidence() {
        var data = new ExtractedResumeData(
                new ExtractedResumeData.PersonalInfo("Ada Lovelace", "ada@example.test", "", "", "", ""),
                List.of(new ExtractedResumeData.Education("BSc", "University", "Computing", "", "", "")),
                List.of("Java", "SQL"),
                List.of("Communication"),
                List.of(new ExtractedResumeData.Experience("WORK", "Developer", "Example Co", "", "", "", "Built apps")),
                List.of(new ExtractedResumeData.Project("API", "Created a web API", List.of("Java"), "")),
                List.of(), List.of("Clear project work"), List.of(), List.of(), List.of(),
                List.of(new ExtractedResumeData.RecommendedRole("Developer", "Skill overlap", List.of("Java"), List.of("Cloud"), "Entry")));

        var score = service.score(data, """
                Ada Lovelace
                Education
                Experience
                Skills
                Projects
                Java SQL Communication
                Built several projects with clear technical responsibilities and collaborative communication.
                Designed maintainable services and documented the implementation for future contributors.
                """);

        assertTrue(score.available());
        assertNotNull(score.overallScore());
        assertEquals(6, score.criteria().size());
        assertTrue(score.criteria().stream().allMatch(item -> item.score() >= 0 && item.score() <= 100));
        assertTrue(score.disclaimer().contains("not an official ATS score"));
    }

    @Test
    void reportsUnavailableWhenEvidenceIsInsufficient() {
        var empty = new ExtractedResumeData(
                new ExtractedResumeData.PersonalInfo("", "", "", "", "", ""),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of());

        var score = service.score(empty, "A short document");

        assertFalse(score.available());
        assertNull(score.overallScore());
        assertEquals("Unavailable", score.label());
    }
}
