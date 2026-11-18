package com.scamshield.jobfinder.service;

import java.util.List;
import java.util.Optional;

import com.scamshield.jobfinder.dto.JobSearchRequest;
import com.scamshield.jobfinder.provider.ConfiguredJobProvider.JobProviderException;
import com.scamshield.jobfinder.provider.JobProvider;
import com.scamshield.jobfinder.provider.JobProviderSearchResult;
import com.scamshield.jobfinder.model.Job;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

@Service
public class JobProviderService {
    private final List<JobProvider> providers;

    public JobProviderService(List<JobProvider> providers) {
        this.providers = List.copyOf(providers);
    }

    public JobProvider configuredProvider() {
        return providers.stream()
                .filter(JobProvider::isConfigured)
                .findFirst()
                .orElseThrow(() -> new JobProviderException(
                        HttpStatusCode.valueOf(503),
                        "Live job search is unavailable. Configure ADZUNA_APP_ID and ADZUNA_APP_KEY on the backend."));
    }

    public JobProviderSearchResult search(JobSearchRequest request) {
        return configuredProvider().search(request);
    }

    public Optional<Job> findById(String sourceName, String sourceJobId, String keyword, String location) {
        JobProvider provider = configuredProvider();
        if (!provider.sourceName().equals(sourceName)) {
            throw new JobProviderException(HttpStatusCode.valueOf(404), "The job was not found in the live provider.");
        }
        return provider.findById(sourceJobId, keyword, location);
    }

    public String sourceName() {
        return providers.stream().findFirst().map(JobProvider::sourceName).orElse("Job provider");
    }

    public String sourceUrl() {
        return providers.stream().findFirst().map(JobProvider::sourceUrl).orElse("https://developer.adzuna.com");
    }

    public boolean isConfigured() {
        return providers.stream().anyMatch(JobProvider::isConfigured);
    }
}
