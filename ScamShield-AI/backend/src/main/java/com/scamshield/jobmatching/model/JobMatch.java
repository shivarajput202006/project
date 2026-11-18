package com.scamshield.jobmatching.model;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import com.scamshield.jobfinder.model.JobType;
import com.scamshield.jobfinder.model.JobWorkMode;
import com.scamshield.jobmatching.dto.MatchCriterionResponse;
import com.scamshield.jobmatching.dto.MatchExplanationResponse;
import com.scamshield.jobmatching.enums.MatchStatus;

@Document(collection = "job_matches")
@CompoundIndex(
        name = "owner_resume_job_match_idx",
        def = "{'ownerId': 1, 'resumeId': 1, 'sourceName': 1, 'sourceJobId': 1}",
        unique = true)
@CompoundIndex(name = "owner_match_history_idx", def = "{'ownerId': 1, 'analyzedAt': -1}")
public class JobMatch {
    @Id
    private String id;

    private String ownerId;
    private String resumeId;
    private String sourceName;
    private String sourceJobId;
    private String searchKeyword;
    private String searchLocation;
    private JobWorkMode preferredWorkMode;
    private JobType preferredJobType;
    private MatchStatus status;
    private Integer score;
    private List<MatchCriterionResponse> scoreBreakdown;
    private List<String> matchingSkills;
    private List<String> missingSkills;
    private MatchExplanationResponse explanation;
    private String scoringModelVersion;
    private Instant analyzedAt;

    protected JobMatch() {
    }

    public JobMatch(
            String ownerId,
            String resumeId,
            String sourceName,
            String sourceJobId,
            String searchKeyword,
            String searchLocation,
            JobWorkMode preferredWorkMode,
            JobType preferredJobType,
            Integer score,
            List<MatchCriterionResponse> scoreBreakdown,
            List<String> matchingSkills,
            List<String> missingSkills,
            MatchExplanationResponse explanation,
            String scoringModelVersion,
            Instant analyzedAt) {
        this.ownerId = ownerId;
        this.resumeId = resumeId;
        this.sourceName = sourceName;
        this.sourceJobId = sourceJobId;
        this.searchKeyword = searchKeyword;
        this.searchLocation = searchLocation;
        this.preferredWorkMode = preferredWorkMode;
        this.preferredJobType = preferredJobType;
        this.status = MatchStatus.MATCHED;
        this.score = score;
        this.scoreBreakdown = List.copyOf(scoreBreakdown);
        this.matchingSkills = List.copyOf(matchingSkills);
        this.missingSkills = List.copyOf(missingSkills);
        this.explanation = explanation;
        this.scoringModelVersion = scoringModelVersion;
        this.analyzedAt = analyzedAt;
    }

    public void updateFrom(JobMatch refreshed) {
        this.searchKeyword = refreshed.searchKeyword;
        this.searchLocation = refreshed.searchLocation;
        this.preferredWorkMode = refreshed.preferredWorkMode;
        this.preferredJobType = refreshed.preferredJobType;
        this.status = refreshed.status;
        this.score = refreshed.score;
        this.scoreBreakdown = refreshed.scoreBreakdown;
        this.matchingSkills = refreshed.matchingSkills;
        this.missingSkills = refreshed.missingSkills;
        this.explanation = refreshed.explanation;
        this.scoringModelVersion = refreshed.scoringModelVersion;
        this.analyzedAt = refreshed.analyzedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOwnerId() { return ownerId; }
    public String getResumeId() { return resumeId; }
    public String getSourceName() { return sourceName; }
    public String getSourceJobId() { return sourceJobId; }
    public String getSearchKeyword() { return searchKeyword; }
    public String getSearchLocation() { return searchLocation; }
    public JobWorkMode getPreferredWorkMode() { return preferredWorkMode; }
    public JobType getPreferredJobType() { return preferredJobType; }
    public MatchStatus getStatus() { return status; }
    public Integer getScore() { return score; }
    public List<MatchCriterionResponse> getScoreBreakdown() { return scoreBreakdown; }
    public List<String> getMatchingSkills() { return matchingSkills; }
    public List<String> getMissingSkills() { return missingSkills; }
    public MatchExplanationResponse getExplanation() { return explanation; }
    public String getScoringModelVersion() { return scoringModelVersion; }
    public Instant getAnalyzedAt() { return analyzedAt; }
}
