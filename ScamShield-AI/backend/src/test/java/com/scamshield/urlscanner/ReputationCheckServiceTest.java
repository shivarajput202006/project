package com.scamshield.urlscanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

class ReputationCheckServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void reportsNotConfiguredWithoutInventingAReputationResult() {
        ReputationCheckService service = service("", new ReputationCheckService.ReputationHttpResponse(200, "{}".getBytes()));

        URLScanResponse.ReputationFinding finding = service.check("https://example.com");

        assertEquals("NOT_CONFIGURED", finding.status());
        assertTrue(finding.threatTypes().isEmpty());
        assertNull(finding.retrievedAt());
    }

    @Test
    void distinguishesNoMatchFromConfirmedMatch() {
        ReputationCheckService cleanService = service(
                "test-key",
                new ReputationCheckService.ReputationHttpResponse(200, "{}".getBytes()));
        ReputationCheckService matchService = service(
                "test-key",
                new ReputationCheckService.ReputationHttpResponse(
                        200,
                        "{\"matches\":[{\"threatType\":\"SOCIAL_ENGINEERING\"}]}".getBytes()));

        URLScanResponse.ReputationFinding clean = cleanService.check("https://example.com");
        URLScanResponse.ReputationFinding match = matchService.check("https://example.com");

        assertEquals("NO_MATCH", clean.status());
        assertNotNull(clean.retrievedAt());
        assertEquals("CONFIRMED_MATCH", match.status());
        assertEquals("SOCIAL_ENGINEERING", match.threatTypes().get(0));
    }

    @Test
    void reportsQuotaAndProviderErrorsAsUnavailable() {
        assertEquals(
                "RATE_LIMITED",
                service("test-key", new ReputationCheckService.ReputationHttpResponse(429, new byte[0]))
                        .check("https://example.com")
                        .status());
        assertEquals(
                "UNAVAILABLE",
                service("test-key", new ReputationCheckService.ReputationHttpResponse(503, new byte[0]))
                        .check("https://example.com")
                        .status());
    }

    @Test
    void reportsTimeoutOrMalformedProviderResponsesAsUnavailable() {
        ReputationCheckService timedOut = new ReputationCheckService("test-key", objectMapper, HttpClient.newHttpClient()) {
            @Override
            protected ReputationHttpResponse sendRequest(HttpRequest request) throws IOException {
                throw new IOException("timeout");
            }
        };
        ReputationCheckService malformed = service(
                "test-key",
                new ReputationCheckService.ReputationHttpResponse(200, "{bad json".getBytes()));

        assertEquals("UNAVAILABLE", timedOut.check("https://example.com").status());
        assertEquals("UNAVAILABLE", malformed.check("https://example.com").status());
    }

    @Test
    void reputationRetrievalTimeIsPresentForProviderResponses() {
        URLScanResponse.ReputationFinding finding = service(
                "test-key",
                new ReputationCheckService.ReputationHttpResponse(200, "{}".getBytes()))
                .check("https://example.com");

        assertTrue(finding.retrievedAt().isBefore(Instant.now().plusSeconds(1)));
    }

    private ReputationCheckService service(String key, ReputationCheckService.ReputationHttpResponse response) {
        return new ReputationCheckService(key, objectMapper, HttpClient.newHttpClient()) {
            @Override
            protected ReputationHttpResponse sendRequest(HttpRequest request) {
                return response;
            }
        };
    }
}
