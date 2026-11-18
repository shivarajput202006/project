package com.scamshield.resume.service;

import org.springframework.stereotype.Service;

import com.scamshield.jobfinder.service.JobProviderService;
import com.scamshield.resume.dto.ResumeRecommendationResponse;
import com.scamshield.resume.model.ResumeAnalysis;

@Service
public class ResumeRecommendationService {
    private final JobProviderService jobProviderService;

    public ResumeRecommendationService(JobProviderService jobProviderService) {
        this.jobProviderService = jobProviderService;
    }

    public ResumeRecommendationResponse recommend(ResumeAnalysis analysis) {
        boolean configured = jobProviderService.isConfigured();
        String message = configured
                ? "Role suggestions are based on resume evidence. Use Live Job Finder for current provider listings."
                : "Role suggestions are based on resume evidence. Live listings are unavailable until the job provider is configured.";
        return new ResumeRecommendationResponse(
                analysis.getResumeId(),
                analysis.getRecommendedRoles(),
                configured,
                false,
                message);
    }
}
