package com.scamshield.jobmatching.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scamshield.jobfinder.model.JobType;
import com.scamshield.jobfinder.model.JobWorkMode;
import com.scamshield.jobmatching.dto.JobMatchHistoryResponse;
import com.scamshield.jobmatching.dto.JobMatchRequest;
import com.scamshield.jobmatching.dto.JobMatchResponse;
import com.scamshield.jobmatching.dto.JobMatchSearchResponse;
import com.scamshield.jobmatching.service.JobMatchingService;
import com.scamshield.security.UserPrincipal;

@Validated
@RestController
@RequestMapping("/api/job-matching")
public class JobMatchingController {
    private final JobMatchingService jobMatchingService;

    public JobMatchingController(JobMatchingService jobMatchingService) {
        this.jobMatchingService = jobMatchingService;
    }

    @PostMapping("/analyze")
    public JobMatchSearchResponse analyze(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody JobMatchRequest request) {
        return jobMatchingService.analyze(principal, request);
    }

    @GetMapping("/recommendations")
    public JobMatchSearchResponse recommendations(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @Size(max = 128) String resumeId,
            @RequestParam(required = false) @Size(max = 120) String keyword,
            @RequestParam(required = false) @Size(max = 100) String location,
            @RequestParam(required = false) JobWorkMode workMode,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(defaultValue = "1") @Min(1) @Max(10000) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return jobMatchingService.recommendations(
                principal, resumeId, keyword, location, workMode, jobType, page, size);
    }

    @GetMapping("/{jobId}")
    public JobMatchResponse details(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String jobId) {
        return jobMatchingService.details(principal, jobId);
    }

    @GetMapping("/history")
    public List<JobMatchHistoryResponse> history(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @Size(max = 128) String resumeId) {
        return jobMatchingService.history(principal, resumeId);
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<Void> deleteHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        jobMatchingService.deleteHistory(principal, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public JobMatchSearchResponse refresh(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody JobMatchRequest request) {
        return jobMatchingService.analyze(principal, request);
    }
}
