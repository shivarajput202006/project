package com.scamshield.resume.dto;

import java.time.Instant;

import com.scamshield.resume.model.Resume;
import com.scamshield.resume.model.ResumeFileType;
import com.scamshield.resume.model.ResumeProcessingStatus;

public record ResumeUploadResponse(
        String id,
        String originalFilename,
        ResumeFileType fileType,
        Instant uploadedAt,
        ResumeProcessingStatus processingStatus) {

    public static ResumeUploadResponse from(Resume resume) {
        return new ResumeUploadResponse(
                resume.getId(),
                resume.getOriginalFilename(),
                resume.getFileType(),
                resume.getUploadedAt(),
                resume.getProcessingStatus());
    }
}
