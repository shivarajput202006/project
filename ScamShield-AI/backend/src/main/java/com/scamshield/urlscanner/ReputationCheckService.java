package com.scamshield.urlscanner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ReputationCheckService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReputationCheckService.class);
    private static final String SOURCE = "Google Safe Browsing";
    private static final String ENDPOINT = "https://safebrowsing.googleapis.com/v4/threatMatches:find";
    private static final int MAX_RESPONSE_BYTES = 64 * 1024;
    private final String apiKey;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public ReputationCheckService(
            @Value("${app.url-scanner.google-safe-browsing-api-key:}") String apiKey,
            ObjectMapper objectMapper) {
        this(apiKey, objectMapper, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build());
    }

    ReputationCheckService(String apiKey, ObjectMapper objectMapper, HttpClient httpClient) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    public URLScanResponse.ReputationFinding check(String submittedUrl) {
        if (apiKey.isBlank()) {
            return finding(
                    "NOT_CONFIGURED",
                    null,
                    List.of(),
                    "No reputation provider is configured. A URL cannot be verified from heuristics alone.");
        }

        try {
            byte[] requestBody = objectMapper.writeValueAsBytes(new ReputationRequest(submittedUrl));
            URI endpoint = URI.create(ENDPOINT + "?key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8));
            HttpRequest request = HttpRequest.newBuilder(endpoint)
                    .timeout(Duration.ofSeconds(5))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                    .build();
            ReputationHttpResponse response = sendRequest(request);
            Instant retrievedAt = Instant.now();
            if (response.body().length > MAX_RESPONSE_BYTES) {
                return finding("UNAVAILABLE", retrievedAt, List.of(), "The reputation provider returned an oversized response.");
            }
            if (response.statusCode() == 429) {
                return finding("RATE_LIMITED", retrievedAt, List.of(), "The reputation provider quota is currently unavailable.");
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return finding("UNAVAILABLE", retrievedAt, List.of(), "The reputation provider could not complete the lookup.");
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode matches = root.path("matches");
            if (!matches.isMissingNode() && !matches.isArray()) {
                return finding("UNAVAILABLE", retrievedAt, List.of(), "The reputation provider response was not understood.");
            }
            List<String> threatTypes = new ArrayList<>();
            if (matches.isArray()) {
                for (JsonNode match : matches) {
                    JsonNode type = match.path("threatType");
                    if (type.isTextual() && !type.asText().isBlank()) {
                        threatTypes.add(type.asText().substring(0, Math.min(type.asText().length(), 80)));
                    }
                }
            }
            if (!threatTypes.isEmpty()) {
                return finding(
                        "CONFIRMED_MATCH",
                        retrievedAt,
                        List.copyOf(threatTypes),
                        "The configured provider returned a threat match. This finding is a strong warning, not a guarantee of complete coverage.");
            }
            return finding(
                    "NO_MATCH",
                    retrievedAt,
                    List.of(),
                    "The provider returned no match at scan time. A clean result does not prove the URL is safe.");
        } catch (IOException exception) {
            LOGGER.warn("URL reputation lookup failed because the provider response could not be read.");
            return finding("UNAVAILABLE", Instant.now(), List.of(), "The reputation provider is unavailable.");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            LOGGER.warn("URL reputation lookup was interrupted.");
            return finding("UNAVAILABLE", Instant.now(), List.of(), "The reputation lookup timed out or was interrupted.");
        } catch (IllegalArgumentException exception) {
            LOGGER.warn("URL reputation lookup could not be completed.");
            return finding("UNAVAILABLE", Instant.now(), List.of(), "The reputation provider response was unavailable.");
        }
    }

    protected ReputationHttpResponse sendRequest(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        try (InputStream body = response.body()) {
            byte[] bytes = body.readNBytes(MAX_RESPONSE_BYTES + 1);
            return new ReputationHttpResponse(response.statusCode(), bytes);
        }
    }

    private URLScanResponse.ReputationFinding finding(
            String status, Instant retrievedAt, List<String> threatTypes, String description) {
        return new URLScanResponse.ReputationFinding(SOURCE, status, retrievedAt, threatTypes, description);
    }

    private record ReputationRequest(
            Client client,
            ThreatInfo threatInfo) {
        ReputationRequest(String submittedUrl) {
            this(
                    new Client("ScamShield AI", "1.0"),
                    new ThreatInfo(
                            List.of("MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE", "POTENTIALLY_HARMFUL_APPLICATION"),
                            List.of("ANY_PLATFORM"),
                            List.of("URL"),
                            List.of(new ThreatEntry(submittedUrl))));
        }
    }

    private record Client(String clientId, String clientVersion) {
    }

    private record ThreatInfo(
            List<String> threatTypes,
            List<String> platformTypes,
            List<String> threatEntryTypes,
            List<ThreatEntry> threatEntries) {
    }

    private record ThreatEntry(String url) {
    }

    protected record ReputationHttpResponse(int statusCode, byte[] body) {
    }
}
