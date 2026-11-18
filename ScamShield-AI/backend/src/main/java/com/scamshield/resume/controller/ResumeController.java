package com.scamshield.resume.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.scamshield.resume.dto.ResumeAnalyzeRequest;
import com.scamshield.resume.dto.ResumeAnalysisResponse;
import com.scamshield.resume.dto.ResumeHistoryItem;
import com.scamshield.resume.dto.ResumeRecommendationResponse;
import com.scamshield.resume.dto.ResumeUploadResponse;
import com.scamshield.resume.service.ResumeAnalysisService;
import com.scamshield.resume.service.ResumeRecommendationService;
import com.scamshield.resume.service.ResumeService;
import com.scamshield.security.UserPrincipal;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {
    private final ResumeService resumeService;
    private final ResumeAnalysisService analysisService;
    private final ResumeRecommendationService recommendationService;

    public ResumeController(
            ResumeService resumeService,
            ResumeAnalysisService analysisService,
            ResumeRecommendationService recommendationService) {
        this.resumeService = resumeService;
        this.analysisService = analysisService;
        this.recommendationService = recommendationService;
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeUploadResponse> upload(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resumeService.upload(principal, file));
    }

    @PostMapping(path = "/{id}/analyze", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResumeAnalysisResponse analyze(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody ResumeAnalyzeRequest request) {
        return analysisService.analyze(principal, id, request);
    }

    @GetMapping("/history")
    public List<ResumeHistoryItem> history(@AuthenticationPrincipal UserPrincipal principal) {
        return resumeService.history(principal);
    }

    @GetMapping("/{id}/analysis")
    public ResumeAnalysisResponse analysis(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        return ResumeAnalysisResponse.from(analysisService.latest(principal, id));
    }

    @GetMapping("/{id}/recommendations")
    public ResumeRecommendationResponse recommendations(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        return recommendationService.recommend(analysisService.latest(principal, id));
    }

    @GetMapping("/{id}")
    public ResumeUploadResponse get(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        return resumeService.get(principal, id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        resumeService.delete(principal, id);
        return ResponseEntity.noContent().build();
    }
}
