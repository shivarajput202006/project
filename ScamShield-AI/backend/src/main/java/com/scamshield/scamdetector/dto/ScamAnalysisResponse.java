package com.scamshield.scamdetector.dto;

import java.time.Instant;
import java.util.List;

import com.scamshield.scamdetector.model.EvidenceItem;
import com.scamshield.scamdetector.model.InputType;
import com.scamshield.scamdetector.model.RiskIndicator;
import com.scamshield.scamdetector.model.RiskStatus;
import com.scamshield.scamdetector.model.ScamAnalysis;

public record ScamAnalysisResponse(
        String id,
        InputType inputType,
        String inputSummary,
        RiskStatus riskStatus,
        int riskScore,
        String summary,
        List<RiskIndicator> indicators,
        List<EvidenceItem> evidence,
        String companyVerificationStatus,
        List<String> recommendations,
        List<String> limitations,
        Instant analysisTimestamp,
        String disclaimer) {
    public static final String DISCLAIMER =
            "AI analysis is advisory and does not guarantee that an opportunity is genuine or fraudulent. "
                    + "Verify important details independently before sharing personal information or making payments.";

    public ScamAnalysisResponse {
        indicators = List.copyOf(indicators);
        evidence = List.copyOf(evidence);
        recommendations = List.copyOf(recommendations);
        limitations = List.copyOf(limitations);
    }

    public static ScamAnalysisResponse from(ScamAnalysis analysis) {
        return new ScamAnalysisResponse(
                analysis.getId(),
                analysis.getInputType(),
                analysis.getInputSummary(),
                analysis.getRiskStatus(),
                analysis.getRiskScore(),
                analysis.getSummary(),
                analysis.getIndicators(),
                analysis.getEvidence(),
                analysis.getCompanyVerificationStatus(),
                analysis.getRecommendations(),
                analysis.getLimitations(),
                analysis.getAnalysisTimestamp(),
                DISCLAIMER);
    }
}
