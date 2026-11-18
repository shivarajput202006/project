package com.scamshield.urlscanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DomainAnalysisServiceTest {
    private final SafeURLValidator validator = new SafeURLValidator();
    private final DomainAnalysisService service = new DomainAnalysisService();

    @Test
    void reportsPotentialLookalikeAsHeuristic() {
        DomainAnalysisService.Analysis result = service.analyze(validator.validate("https://paypa1.example/apply"));

        assertTrue(result.indicators().stream().anyMatch(indicator ->
                indicator.code().equals("LOOKALIKE_LABEL") && indicator.heuristic()));
    }

    @Test
    void doesNotFlagHttpOrHyphensAlone() {
        DomainAnalysisService.Analysis result =
                service.analyze(validator.validate("http://ordinary-company.example/apply"));

        assertTrue(result.indicators().isEmpty());
    }

    @Test
    void flagsCommonShortenersAndEncodedSeparatorsAsHeuristics() {
        DomainAnalysisService.Analysis result = service.analyze(
                validator.validate("https://bit.ly/jobs%2fapply"));

        assertTrue(result.indicators().stream().anyMatch(indicator -> indicator.code().equals("URL_SHORTENER")));
        assertTrue(result.indicators().stream().anyMatch(indicator ->
                indicator.code().equals("SUSPICIOUS_URL_CHARACTERS")));
        assertFalse(result.indicators().stream().anyMatch(indicator -> !indicator.heuristic()));
    }

    @Test
    void flagsMixedWritingSystemsInAnInternationalizedHostname() {
        DomainAnalysisService.Analysis result = service.analyze(
                validator.validate("https://pаypal.example"));

        assertTrue(result.indicators().stream().anyMatch(indicator -> indicator.code().equals("MIXED_SCRIPT")));
    }
}
