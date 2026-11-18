package com.scamshield.resume.model;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import com.scamshield.resume.dto.ExtractedResumeData;
import com.scamshield.resume.dto.ResumeScoreResponse;
import com.scamshield.resume.dto.SkillAnalysisResponse;

@Document(collection = "resume_analyses")
@CompoundIndex(
        name = "resume_analysis_owner_time_idx",
        def = "{'ownerId': 1, 'resumeId': 1, 'analysisTimestamp': -1}")
public class ResumeAnalysis {
    @Id
    private String id;

    private String resumeId;
    private String ownerId;
    private ExtractedResumeData extractedData;
    private SkillAnalysisResponse skillAnalysis;
    private ResumeScoreResponse score;
    private List<String> atsFeedback;
    private List<ExtractedResumeData.ImprovementSuggestion> improvementSuggestions;
    private List<ExtractedResumeData.RecommendedRole> recommendedRoles;
    private Instant analysisTimestamp;
    private List<String> limitations;

    protected ResumeAnalysis() {
    }

    public ResumeAnalysis(
            String resumeId,
            String ownerId,
            ExtractedResumeData extractedData,
            SkillAnalysisResponse skillAnalysis,
            ResumeScoreResponse score,
            Instant analysisTimestamp,
            List<String> limitations) {
        this.resumeId = resumeId;
        this.ownerId = ownerId;
        this.extractedData = extractedData;
        this.skillAnalysis = skillAnalysis;
        this.score = score;
        this.atsFeedback = List.copyOf(extractedData.atsFeedback());
        this.improvementSuggestions = List.copyOf(extractedData.improvementSuggestions());
        this.recommendedRoles = List.copyOf(extractedData.recommendedRoles());
        this.analysisTimestamp = analysisTimestamp;
        this.limitations = List.copyOf(limitations);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResumeId() {
        return resumeId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public ExtractedResumeData getExtractedData() {
        return extractedData;
    }

    public SkillAnalysisResponse getSkillAnalysis() {
        return skillAnalysis;
    }

    public ResumeScoreResponse getScore() {
        return score;
    }

    public List<String> getAtsFeedback() {
        return atsFeedback;
    }

    public List<ExtractedResumeData.ImprovementSuggestion> getImprovementSuggestions() {
        return improvementSuggestions;
    }

    public List<ExtractedResumeData.RecommendedRole> getRecommendedRoles() {
        return recommendedRoles;
    }

    public Instant getAnalysisTimestamp() {
        return analysisTimestamp;
    }

    public List<String> getLimitations() {
        return limitations;
    }
}
