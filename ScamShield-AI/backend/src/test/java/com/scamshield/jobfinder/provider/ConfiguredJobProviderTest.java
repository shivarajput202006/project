package com.scamshield.jobfinder.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.scamshield.jobfinder.dto.JobSearchRequest;

class ConfiguredJobProviderTest {
    @Test
    void missingCredentialsMakeTheProviderExplicitlyUnavailable() {
        ConfiguredJobProvider provider = new ConfiguredJobProvider("", "", "gb", RestClient.builder());

        assertFalse(provider.isConfigured());
        ConfiguredJobProvider.JobProviderException exception = assertThrows(
                ConfiguredJobProvider.JobProviderException.class,
                () -> provider.search(new JobSearchRequest(null, null, null, null, null, null, 1, 20)));
        assertEquals(503, exception.getStatus().value());
        assertEquals(
                "Live job search is unavailable. Configure ADZUNA_APP_ID and ADZUNA_APP_KEY on the backend.",
                exception.getMessage());
    }

    @Test
    void invalidCountryConfigurationDoesNotEnableProviderRequests() {
        ConfiguredJobProvider provider = new ConfiguredJobProvider("id", "key", "../", RestClient.builder());

        assertFalse(provider.isConfigured());
    }
}
