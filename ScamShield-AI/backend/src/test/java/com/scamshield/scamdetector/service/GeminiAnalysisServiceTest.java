package com.scamshield.scamdetector.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.List;

import com.sun.net.httpserver.HttpServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scamshield.scamdetector.exception.ScamDetectorException;
import com.scamshield.scamdetector.model.EvidenceType;
import com.scamshield.scamdetector.model.InputType;
import com.scamshield.scamdetector.model.RiskStatus;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GeminiAnalysisServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void reportsUnavailableWhenApiKeyIsNotConfigured() {
        GeminiAnalysisService service = new GeminiAnalysisService(
                new ObjectMapper(),
                "",
                "gemini-2.5-flash",
                Duration.ofSeconds(1),
                "http://127.0.0.1:1/v1beta");

        ScamDetectorException exception = assertThrows(
                ScamDetectorException.class,
                () -> service.analyze(InputType.TEXT, "message", null, null, List.of()));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
    }

    @Test
    void rejectsMalformedAiOutputInsteadOfReturningASuccessfulReport() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1beta/models/gemini-2.5-flash:generateContent", exchange -> {
            byte[] body = """
                    {"candidates":[{"content":{"parts":[{"text":"not JSON"}]}}]}
                    """.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });
        server.start();
        try {
            GeminiAnalysisService service = new GeminiAnalysisService(
                    new ObjectMapper(),
                    "test-key",
                    "gemini-2.5-flash",
                    Duration.ofSeconds(2),
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/v1beta");

            ScamDetectorException exception = assertThrows(
                    ScamDetectorException.class,
                    () -> service.analyze(InputType.TEXT, "message", null, null, List.of()));

            assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatus());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void parsesValidatedStructuredAnalysisAndKeepsCompanyUnverified() throws Exception {
        String report = objectMapper.writeValueAsString(Map.of(
                "riskStatus", "NEEDS_REVIEW",
                "riskScore", 44,
                "summary", "The request includes details that need checking.",
                "indicators", List.of(Map.of(
                        "category", "Payment",
                        "title", "Fee request",
                        "details", "The message asks the applicant to pay before employment.",
                        "evidenceType", "USER_PROVIDED")),
                "evidence", List.of(Map.of(
                        "evidenceType", "USER_PROVIDED",
                        "description", "A fee is requested before an interview.")),
                "companyVerificationStatus", "UNVERIFIED",
                "recommendations", List.of("Verify the fee with an independently located company contact."),
                "limitations", List.of()));
        String providerResponse = objectMapper.writeValueAsString(Map.of(
                "candidates",
                List.of(Map.of("content", Map.of("parts", List.of(Map.of("text", report)))))));
        HttpServer server = serverReturning(providerResponse, 200);
        server.start();
        try {
            GeminiAnalysisService service = new GeminiAnalysisService(
                    objectMapper,
                    "test-key",
                    "gemini-2.5-flash",
                    Duration.ofSeconds(2),
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/v1beta");

            var result = service.analyze(InputType.TEXT, "untrusted content", null, null, List.of());

            assertEquals(RiskStatus.NEEDS_REVIEW, result.riskStatus());
            assertEquals(44, result.riskScore());
            assertEquals("UNVERIFIED", result.companyVerificationStatus());
            assertEquals(EvidenceType.USER_PROVIDED, result.indicators().get(0).evidenceType());
            assertTrue(result.limitations().stream().anyMatch(value -> value.contains("No trusted")));
        } finally {
            server.stop(0);
        }
    }

    private HttpServer serverReturning(String body, int status) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1beta/models/gemini-2.5-flash:generateContent", exchange -> {
            byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            try (var output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        });
        return server;
    }
}
