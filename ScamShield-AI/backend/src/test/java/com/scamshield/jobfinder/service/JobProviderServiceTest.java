package com.scamshield.jobfinder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.scamshield.jobfinder.provider.ConfiguredJobProvider.JobProviderException;
import com.scamshield.jobfinder.provider.JobProvider;

class JobProviderServiceTest {
    @Test
    void reportsProviderUnavailableInsteadOfReturningEmptyOrFabricatedResults() {
        JobProvider provider = new JobProvider() {
            @Override
            public String sourceName() { return "Adzuna"; }

            @Override
            public String sourceUrl() { return "https://www.adzuna.co.uk"; }

            @Override
            public boolean isConfigured() { return false; }

            @Override
            public com.scamshield.jobfinder.provider.JobProviderSearchResult search(
                    com.scamshield.jobfinder.dto.JobSearchRequest request) {
                throw new AssertionError("An unconfigured provider must not be called.");
            }

            @Override
            public java.util.Optional<com.scamshield.jobfinder.model.Job> findById(
                    String sourceJobId, String keyword, String location) {
                throw new AssertionError("An unconfigured provider must not be called.");
            }
        };
        JobProviderService service = new JobProviderService(List.of(provider));

        JobProviderException exception = assertThrows(
                JobProviderException.class,
                () -> service.search(new com.scamshield.jobfinder.dto.JobSearchRequest(
                        "Developer", "London", null, null, null, null, 1, 20)));

        assertEquals(503, exception.getStatus().value());
        assertEquals("Adzuna", service.sourceName());
        assertEquals(false, service.isConfigured());
    }
}
