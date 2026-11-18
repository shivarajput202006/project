package com.scamshield.resume.dto;

import java.time.Instant;

import com.scamshield.resume.model.Resume;
import com.scamshield.resume.model.ResumeFileType;
import com.scamshield.resume.model.ResumeProcessingStatus;

public record ResumeHistoryItem(
        String id,
        String originalFilename,
        ResumeFileType fileType,
        Instant uploadedAt,
        ResumeProcessingStatus processingStatus,
        Instant latestAnalysisAt,
        Integer latestScore) {

    public static ResumeHistoryItem from(Resume resume, Instant latestAnalysisAt, Integer latestScore) {
        return new ResumeHistoryItem(
                resume.getId(),
                resume.getOriginalFilename(),
                resume.getFileType(),
                resume.getUploadedAt(),
                resume.getProcessingStatus(),
                latestAnalysisAt,
                latestScore);
    }
}
