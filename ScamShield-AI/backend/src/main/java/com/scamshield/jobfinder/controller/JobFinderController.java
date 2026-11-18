package com.scamshield.jobfinder.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import com.scamshield.jobfinder.dto.JobFiltersResponse;
import com.scamshield.jobfinder.dto.JobRecommendationResponse;
import com.scamshield.jobfinder.dto.JobResponse;
import com.scamshield.jobfinder.dto.JobSaveRequest;
import com.scamshield.jobfinder.dto.JobSearchRequest;
import com.scamshield.jobfinder.dto.JobSearchResponse;
import com.scamshield.jobfinder.model.JobExperience;
import com.scamshield.jobfinder.model.JobType;
import com.scamshield.jobfinder.model.JobWorkMode;
import com.scamshield.jobfinder.service.JobFinderService;
import com.scamshield.jobfinder.service.JobSearchRateLimiter;
import com.scamshield.security.UserPrincipal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/jobs")
public class JobFinderController {
    private final JobFinderService jobFinderService;

    public JobFinderController(JobFinderService jobFinderService) {
        this.jobFinderService = jobFinderService;
    }

    @GetMapping("/search")
    public JobSearchResponse search(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @Size(max = 120) String keyword,
            @RequestParam(required = false) @Size(max = 100) String location,
            @RequestParam(required = false) JobWorkMode workMode,
            @RequestParam(required = false) JobExperience experience,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) @Min(0) @Max(10000000) Integer salaryMin,
            @RequestParam(defaultValue = "1") @Min(1) @Max(10000) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return jobFinderService.search(
                principal,
                new JobSearchRequest(keyword, location, workMode, experience, jobType, salaryMin, page, size));
    }

    @GetMapping("/filters")
    public JobFiltersResponse filters() {
        return jobFinderService.filters();
    }

    @GetMapping("/saved")
    public List<JobResponse> saved(@AuthenticationPrincipal UserPrincipal principal) {
        return jobFinderService.saved(principal);
    }

    @GetMapping("/recommendations")
    public JobRecommendationResponse recommendations(@AuthenticationPrincipal UserPrincipal principal) {
        return jobFinderService.recommendations(principal);
    }

    @PostMapping("/{id}/save")
    public JobResponse save(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody JobSaveRequest request) {
        return jobFinderService.save(principal, id, request);
    }

    @DeleteMapping("/{id}/save")
    public ResponseEntity<Void> unsave(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        jobFinderService.unsave(principal, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public JobResponse details(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        return jobFinderService.details(principal, id);
    }
}
