package com.scamshield.resume.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.scamshield.resume.dto.ResumeHistoryItem;
import com.scamshield.resume.dto.ResumeUploadResponse;
import com.scamshield.resume.model.Resume;
import com.scamshield.resume.model.ResumeFileType;
import com.scamshield.resume.repository.ResumeAnalysisRepository;
import com.scamshield.resume.repository.ResumeRepository;
import com.scamshield.resume.util.ResumeException;
import com.scamshield.resume.util.ResumeFileValidator;
import com.scamshield.security.UserPrincipal;
import com.scamshield.jobmatching.repository.JobMatchRepository;

@Service
public class ResumeService {
    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository analysisRepository;
    private final GridFsTemplate gridFsTemplate;
    private final ResumeFileValidator fileValidator;
    private final JobMatchRepository jobMatchRepository;

    public ResumeService(
            ResumeRepository resumeRepository,
            ResumeAnalysisRepository analysisRepository,
            GridFsTemplate gridFsTemplate,
            ResumeFileValidator fileValidator,
            JobMatchRepository jobMatchRepository) {
        this.resumeRepository = resumeRepository;
        this.analysisRepository = analysisRepository;
        this.gridFsTemplate = gridFsTemplate;
        this.fileValidator = fileValidator;
        this.jobMatchRepository = jobMatchRepository;
    }

    public ResumeUploadResponse upload(UserPrincipal principal, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ResumeException.badRequest("Choose a non-empty PDF or DOCX resume.");
        }
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException exception) {
            throw ResumeException.badRequest("The uploaded file could not be read.");
        }
        ResumeFileType fileType = fileValidator.validate(
                file.getOriginalFilename(), file.getContentType(), file.getSize(), content);
        String ownerId = authenticatedUserId(principal);
        String storedContentType = fileType == ResumeFileType.PDF
                ? "application/pdf"
                : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        ObjectId storageId = gridFsTemplate.store(
                new ByteArrayInputStream(content),
                file.getOriginalFilename(),
                storedContentType,
                new Document("ownerId", ownerId));
        Resume resume = new Resume(
                ownerId,
                file.getOriginalFilename(),
                fileType,
                storageId.toHexString(),
                Instant.now());
        try {
            return ResumeUploadResponse.from(resumeRepository.save(resume));
        } catch (RuntimeException exception) {
            try {
                gridFsTemplate.delete(Query.query(Criteria.where("_id").is(storageId)));
            } catch (RuntimeException cleanupFailure) {
                exception.addSuppressed(cleanupFailure);
            }
            throw exception;
        }
    }

    public ResumeUploadResponse get(UserPrincipal principal, String resumeId) {
        return ResumeUploadResponse.from(getOwnedResume(resumeId, authenticatedUserId(principal)));
    }

    public List<ResumeHistoryItem> history(UserPrincipal principal) {
        String ownerId = authenticatedUserId(principal);
        List<Resume> resumes = resumeRepository.findTop25ByOwnerIdOrderByUploadedAtDesc(ownerId);
        if (resumes.isEmpty()) return List.of();
        List<String> resumeIds = resumes.stream().map(Resume::getId).toList();
        Map<String, com.scamshield.resume.model.ResumeAnalysis> latestAnalyses = new HashMap<>();
        for (var analysis : analysisRepository.findByOwnerIdAndResumeIdInOrderByAnalysisTimestampDesc(
                ownerId, resumeIds)) {
            latestAnalyses.putIfAbsent(analysis.getResumeId(), analysis);
        }
        List<ResumeHistoryItem> history = new ArrayList<>();
        for (Resume resume : resumes) {
            var analysis = latestAnalyses.get(resume.getId());
            history.add(ResumeHistoryItem.from(
                    resume,
                    analysis == null ? null : analysis.getAnalysisTimestamp(),
                    analysis == null || analysis.getScore() == null
                            ? null
                            : analysis.getScore().overallScore()));
        }
        return List.copyOf(history);
    }

    public void delete(UserPrincipal principal, String resumeId) {
        String ownerId = authenticatedUserId(principal);
        Resume resume = getOwnedResume(resumeId, ownerId);
        ObjectId storageId;
        try {
            storageId = new ObjectId(resume.getStorageReference());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Resume storage reference is invalid.", exception);
        }
        gridFsTemplate.delete(Query.query(
                Criteria.where("_id").is(storageId).and("metadata.ownerId").is(ownerId)));
        analysisRepository.deleteByResumeIdAndOwnerId(resumeId, ownerId);
        jobMatchRepository.deleteByOwnerIdAndResumeId(ownerId, resumeId);
        resumeRepository.deleteByIdAndOwnerId(resumeId, ownerId);
    }

    public Resume getOwnedResume(String resumeId, String ownerId) {
        return resumeRepository.findByIdAndOwnerId(resumeId, ownerId)
                .orElseThrow(ResumeException::notFound);
    }

    public byte[] readStoredFile(Resume resume, String ownerId) {
        ObjectId storageId;
        try {
            storageId = new ObjectId(resume.getStorageReference());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Resume storage reference is invalid.", exception);
        }
        GridFSFile storedFile = gridFsTemplate.findOne(Query.query(
                Criteria.where("_id").is(storageId).and("metadata.ownerId").is(ownerId)));
        if (storedFile == null) {
            throw new ResumeException(HttpStatus.INTERNAL_SERVER_ERROR, "The private resume file is unavailable.");
        }
        try (var input = gridFsTemplate.getResource(storedFile).getInputStream()) {
            return input.readAllBytes();
        } catch (IOException exception) {
            throw new ResumeException(HttpStatus.INTERNAL_SERVER_ERROR, "The private resume file could not be read.");
        }
    }

    public String authenticatedUserId(UserPrincipal principal) {
        String userId = principal == null || principal.getUser() == null
                ? null
                : principal.getUser().getId();
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("Authenticated user has no database identity.");
        }
        return userId;
    }
}
