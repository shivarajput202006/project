package com.scamshield.scamdetector.model;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "scam_analyses")
@CompoundIndex(name = "user_analysis_timestamp_idx", def = "{'userId': 1, 'analysisTimestamp': -1}")
public class ScamAnalysis {
    @Id
    private String id;

    private String userId;
    private InputType inputType;
    private String inputSummary;
    private RiskStatus riskStatus;
    private int riskScore;
    private String summary;
    private List<RiskIndicator> indicators;
    private List<EvidenceItem> evidence;
    private String companyVerificationStatus;
    private List<String> recommendations;
    private List<String> limitations;
    private Instant analysisTimestamp;

    protected ScamAnalysis() {
    }

    public ScamAnalysis(
            String userId,
            InputType inputType,
            String inputSummary,
            AiAnalysisResult result,
            Instant analysisTimestamp) {
        this.userId = userId;
        this.inputType = inputType;
        this.inputSummary = inputSummary;
        this.riskStatus = result.riskStatus();
        this.riskScore = result.riskScore();
        this.summary = result.summary();
        this.indicators = List.copyOf(result.indicators());
        this.evidence = List.copyOf(result.evidence());
        this.companyVerificationStatus = "UNVERIFIED";
        this.recommendations = List.copyOf(result.recommendations());
        this.limitations = List.copyOf(result.limitations());
        this.analysisTimestamp = analysisTimestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public InputType getInputType() {
        return inputType;
    }

    public String getInputSummary() {
        return inputSummary;
    }

    public RiskStatus getRiskStatus() {
        return riskStatus;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public String getSummary() {
        return summary;
    }

    public List<RiskIndicator> getIndicators() {
        return indicators;
    }

    public List<EvidenceItem> getEvidence() {
        return evidence;
    }

    public String getCompanyVerificationStatus() {
        return companyVerificationStatus;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public List<String> getLimitations() {
        return limitations;
    }

    public Instant getAnalysisTimestamp() {
        return analysisTimestamp;
    }
}
