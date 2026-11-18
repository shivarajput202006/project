package com.scamshield.scamdetector.model;

import java.util.List;

public record AiAnalysisResult(
        RiskStatus riskStatus,
        int riskScore,
        String summary,
        List<RiskIndicator> indicators,
        List<EvidenceItem> evidence,
        String companyVerificationStatus,
        List<String> recommendations,
        List<String> limitations) {
    public AiAnalysisResult {
        indicators = List.copyOf(indicators);
        evidence = List.copyOf(evidence);
        recommendations = List.copyOf(recommendations);
        limitations = List.copyOf(limitations);
    }
}
