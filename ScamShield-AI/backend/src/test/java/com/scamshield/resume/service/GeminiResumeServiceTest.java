package com.scamshield.resume.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;

import com.sun.net.httpserver.HttpServer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scamshield.resume.util.ResumeException;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GeminiResumeServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String RESUME_TEXT = """
            Ada Lovelace
            ada@example.test
            Java developer
            Education
            BSc Computer Science
            Experience
            Java at Example Company
            Projects
            Built a Java API for internal reporting
            """;

    @Test
    void returnsUnavailableWithoutAnApiKey() {
        var service = service("", "http://127.0.0.1:1/v1beta", Duration.ofSeconds(1));

        ResumeException exception = assertThrows(ResumeException.class, () -> service.analyze(RESUME_TEXT));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
    }

    @Test
    void parsesStructuredResponseAndRemovesFactsNotSupportedByResume() throws Exception {
        HttpServer server = serverReturning(providerResponse(validReport()), 200, true);
        server.start();
        try {
            var service = service("test-key", baseUrl(server), Duration.ofSeconds(2));

            var result = service.analyze(RESUME_TEXT);

            assertEquals("Ada Lovelace", result.personalInfo().name());
            assertEquals(List.of("Java"), result.technicalSkills());
            assertTrue(result.softSkills().isEmpty());
            assertTrue(result.experience().isEmpty());
            assertEquals(List.of("Java"), result.recommendedRoles().get(0).matchingSkills());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void rejectsMalformedModelJsonInsteadOfSavingAnAnalysis() throws Exception {
        HttpServer server = serverReturning(
                "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"not JSON\"}]}}]}",
                200,
                false);
        server.start();
        try {
            var service = service("test-key", baseUrl(server), Duration.ofSeconds(2));

            ResumeException exception = assertThrows(ResumeException.class, () -> service.analyze(RESUME_TEXT));

            assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatus());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void reportsGeminiUnavailableWithoutLeakingProviderResponse() throws Exception {
        HttpServer server = serverReturning("{\"error\":\"private provider detail\"}", 503, false);
        server.start();
        try {
            var service = service("test-key", baseUrl(server), Duration.ofSeconds(2));

            ResumeException exception = assertThrows(ResumeException.class, () -> service.analyze(RESUME_TEXT));

            assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
            assertTrue(!exception.getMessage().contains("private provider detail"));
        } finally {
            server.stop(0);
        }
    }

    private GeminiResumeService service(String key, String baseUrl, Duration timeout) {
        return new GeminiResumeService(objectMapper, key, "gemini-2.5-flash", timeout, baseUrl);
    }

    private HttpServer serverReturning(String body, int status, boolean inspectSchema) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1beta/models/gemini-2.5-flash:generateContent", exchange -> {
            if (inspectSchema) {
                try {
                    JsonNode request = objectMapper.readTree(exchange.getRequestBody().readAllBytes());
                    boolean schemaOk = request.path("generationConfig").path("responseSchema")
                            .path("properties").path("education").path("type").asText().equals("ARRAY");
                    if (!schemaOk) {
                        exchange.sendResponseHeaders(400, -1);
                        return;
                    }
                } catch (Exception exception) {
                    exchange.sendResponseHeaders(400, -1);
                    return;
                }
            }
            byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            try (var output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        });
        return server;
    }

    private String baseUrl(HttpServer server) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1beta";
    }

    private String providerResponse(String report) throws Exception {
        return objectMapper.writeValueAsString(java.util.Map.of(
                "candidates",
                List.of(java.util.Map.of("content", java.util.Map.of(
                        "parts", List.of(java.util.Map.of("text", report)))))));
    }

    private String validReport() {
        return """
                {
                  "personalInfo":{"name":"Ada Lovelace","email":"ada@example.test","phone":"","location":"","linkedin":"","portfolio":""},
                  "education":[{"degree":"BSc Computer Science","institution":"Example University","fieldOfStudy":"Computer Science","startDate":"","endDate":"","details":""}],
                  "technicalSkills":["Java","Rust"],
                  "softSkills":["Excellent communication"],
                  "experience":[{"kind":"WORK","title":"Senior Engineer","company":"Invented Corp","location":"","startDate":"","endDate":"","summary":"Led a large team."}],
                  "projects":[{"name":"Reporting API","description":"Built a Java API for internal reporting.","technologies":["Java","Kotlin"],"url":""}],
                  "certifications":["Cloud Expert"],
                  "strengths":["Clear technical project evidence"],
                  "weaknesses":["Add measurable outcomes where accurate"],
                  "atsFeedback":["Use clear section headings."],
                  "improvementSuggestions":[{"category":"Experience","suggestion":"Clarify contributions","reason":"Some outcomes are not stated."}],
                  "recommendedRoles":[{"title":"Java Developer","rationale":"Java is explicitly listed.","matchingSkills":["Java","Rust"],"missingSkills":["Kubernetes"],"experienceLevel":"Entry level"}]
                }
                """;
    }
}
