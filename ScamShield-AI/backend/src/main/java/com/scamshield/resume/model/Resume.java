package com.scamshield.resume.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "resumes")
@CompoundIndex(name = "resume_owner_upload_idx", def = "{'ownerId': 1, 'uploadedAt': -1}")
public class Resume {
    @Id
    private String id;

    private String ownerId;
    private String originalFilename;
    private ResumeFileType fileType;
    private String storageReference;
    private Instant uploadedAt;
    private ResumeProcessingStatus processingStatus;

    protected Resume() {
    }

    public Resume(
            String ownerId,
            String originalFilename,
            ResumeFileType fileType,
            String storageReference,
            Instant uploadedAt) {
        this.ownerId = ownerId;
        this.originalFilename = originalFilename;
        this.fileType = fileType;
        this.storageReference = storageReference;
        this.uploadedAt = uploadedAt;
        this.processingStatus = ResumeProcessingStatus.UPLOADED;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public ResumeFileType getFileType() {
        return fileType;
    }

    public String getStorageReference() {
        return storageReference;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public ResumeProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void markAnalyzed() {
        this.processingStatus = ResumeProcessingStatus.ANALYZED;
    }
}
