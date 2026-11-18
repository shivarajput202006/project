package com.scamshield.urlscanner;

import java.time.Instant;
import java.util.List;

public record URLScanResponse(
        String id,
        String submittedUrl,
        String normalizedDomain,
        DomainInformation domainInformation,
        URLRiskStatus riskStatus,
        int riskScore,
        List<URLRiskIndicator> securityIndicators,
        ReputationFinding reputationFinding,
        RedirectSummary redirectSummary,
        List<String> recommendations,
        Instant scanTimestamp) {
    public URLScanResponse {
        securityIndicators = List.copyOf(securityIndicators);
        recommendations = List.copyOf(recommendations);
    }

    public record DomainInformation(
            String scheme,
            String hostnameType,
            String registrationStatus,
            String note) {
    }

    public record ReputationFinding(
            String sourceName,
            String status,
            Instant retrievedAt,
            List<String> threatTypes,
            String description) {
        public ReputationFinding {
            threatTypes = List.copyOf(threatTypes);
        }
    }

    public record RedirectSummary(String status, Integer redirectCount, String detail) {
    }
}
