package com.scamshield.resume.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mock.web.MockMultipartFile;

import com.scamshield.resume.model.Resume;
import com.scamshield.resume.model.ResumeFileType;
import com.scamshield.resume.model.ResumeProcessingStatus;
import com.scamshield.resume.repository.ResumeAnalysisRepository;
import com.scamshield.resume.repository.ResumeRepository;
import com.scamshield.resume.util.ResumeException;
import com.scamshield.resume.util.ResumeFileValidator;
import com.scamshield.jobmatching.repository.JobMatchRepository;
import com.scamshield.security.UserPrincipal;
import com.scamshield.user.User;
import com.scamshield.user.UserRole;

class ResumeServiceTest {
    private final ResumeRepository resumeRepository = org.mockito.Mockito.mock(ResumeRepository.class);
    private final ResumeAnalysisRepository analysisRepository =
            org.mockito.Mockito.mock(ResumeAnalysisRepository.class);
    private final GridFsTemplate gridFsTemplate = org.mockito.Mockito.mock(GridFsTemplate.class);
    private final JobMatchRepository jobMatchRepository = org.mockito.Mockito.mock(JobMatchRepository.class);
    private final ResumeService service = new ResumeService(
            resumeRepository, analysisRepository, gridFsTemplate, new ResumeFileValidator(5 * 1024 * 1024),
            jobMatchRepository);

    @Test
    void storesValidPdfInPrivateGridFsAndPersistsOnlyItsReference() {
        ObjectId storageId = new ObjectId();
        byte[] content = "%PDF-1.7\n".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", content);
        when(gridFsTemplate.store(
                any(InputStream.class), eq("resume.pdf"), eq("application/pdf"), any(Document.class)))
                .thenReturn(storageId);
        when(resumeRepository.save(any(Resume.class))).thenAnswer(invocation -> {
            Resume saved = invocation.getArgument(0);
            saved.setId("resume-id");
            return saved;
        });

        var result = service.upload(principal("owner-1"), file);

        assertEquals("resume-id", result.id());
        assertEquals(ResumeFileType.PDF, result.fileType());
        assertEquals(ResumeProcessingStatus.UPLOADED, result.processingStatus());
        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    void historyIsScopedToCurrentOwnerAndContainsOnlyLatestScores() {
        Resume resume = new Resume(
                "owner-1", "resume.pdf", ResumeFileType.PDF, new ObjectId().toHexString(), Instant.now());
        resume.setId("resume-1");
        when(resumeRepository.findTop25ByOwnerIdOrderByUploadedAtDesc("owner-1")).thenReturn(List.of(resume));
        var data = new com.scamshield.resume.dto.ExtractedResumeData(
                new com.scamshield.resume.dto.ExtractedResumeData.PersonalInfo("", "", "", "", "", ""),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of());
        var score = new com.scamshield.resume.dto.ResumeScoreResponse(true, 72, "Advisory", List.of(), "Not official");
        var analysis = new com.scamshield.resume.model.ResumeAnalysis(
                "resume-1", "owner-1", data,
                new com.scamshield.resume.dto.SkillAnalysisResponse(List.of(), List.of(), List.of()),
                score, Instant.parse("2026-01-02T00:00:00Z"), List.of());
        when(analysisRepository.findByOwnerIdAndResumeIdInOrderByAnalysisTimestampDesc(
                eq("owner-1"), eq(List.of("resume-1")))).thenReturn(List.of(analysis));

        var result = service.history(principal("owner-1"));

        assertEquals(1, result.size());
        assertEquals("resume-1", result.get(0).id());
        assertEquals(72, result.get(0).latestScore());
        verify(resumeRepository).findTop25ByOwnerIdOrderByUploadedAtDesc("owner-1");
    }

    @Test
    void doesNotAllowAnotherUserToReadOrDeleteResume() {
        when(resumeRepository.findByIdAndOwnerId("resume-1", "owner-2")).thenReturn(Optional.empty());

        assertThrows(ResumeException.class, () -> service.get(principal("owner-2"), "resume-1"));
        assertThrows(ResumeException.class, () -> service.delete(principal("owner-2"), "resume-1"));

        verify(gridFsTemplate, never()).delete(any(Query.class));
    }

    @Test
    void deletesOwnedFileAndAllAnalysisRecords() {
        ObjectId storageId = new ObjectId();
        Resume resume = new Resume(
                "owner-1", "resume.pdf", ResumeFileType.PDF, storageId.toHexString(), Instant.now());
        resume.setId("resume-1");
        when(resumeRepository.findByIdAndOwnerId("resume-1", "owner-1")).thenReturn(Optional.of(resume));

        service.delete(principal("owner-1"), "resume-1");

        verify(gridFsTemplate).delete(any(Query.class));
        verify(analysisRepository).deleteByResumeIdAndOwnerId("resume-1", "owner-1");
        verify(jobMatchRepository).deleteByOwnerIdAndResumeId("owner-1", "resume-1");
        verify(resumeRepository).deleteByIdAndOwnerId("resume-1", "owner-1");
    }

    @Test
    void rejectsInvalidContentBeforeWritingToGridFs() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", "not a pdf".getBytes());

        assertThrows(ResumeException.class, () -> service.upload(principal("owner-1"), file));

        verify(gridFsTemplate, never()).store(
                any(InputStream.class), any(String.class), any(String.class), any(Document.class));
    }

    private UserPrincipal principal(String id) {
        User user = new User("Test User", id + "@example.test", "password-hash", UserRole.USER, Instant.now());
        user.setId(id);
        return new UserPrincipal(user);
    }
}
