package com.scamshield.jobfinder.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "saved_jobs")
@CompoundIndex(
        name = "saved_job_user_job_unique_idx",
        def = "{'userId': 1, 'jobId': 1}",
        unique = true)
public class SavedJob {
    @Id
    private String id;

    private String userId;
    private String jobId;
    private Instant savedAt;

    protected SavedJob() {
    }

    public SavedJob(String userId, String jobId, Instant savedAt) {
        this.userId = userId;
        this.jobId = jobId;
        this.savedAt = savedAt;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getJobId() { return jobId; }
    public Instant getSavedAt() { return savedAt; }
}
