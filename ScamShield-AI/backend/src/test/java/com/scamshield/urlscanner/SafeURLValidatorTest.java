package com.scamshield.urlscanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SafeURLValidatorTest {
    private final SafeURLValidator validator = new SafeURLValidator();

    @Test
    void acceptsStandardHttpAndHttpsUrls() {
        assertEquals("example.com", validator.validate("http://example.com/apply").normalizedDomain());
        assertEquals("example.com", validator.validate("https://example.com/apply").normalizedDomain());
    }

    @Test
    void normalizesInternationalizedDomainsToAscii() {
        assertEquals("xn--mnich-kva.example", validator.validate("https://münich.example").normalizedDomain());
    }

    @Test
    void rejectsMalformedUrlsUnsupportedProtocolsAndNonstandardPorts() {
        assertThrows(SafeURLValidator.InvalidUrlException.class, () -> validator.validate("https://"));
        assertThrows(SafeURLValidator.InvalidUrlException.class, () -> validator.validate("file:///etc/passwd"));
        assertThrows(SafeURLValidator.InvalidUrlException.class, () -> validator.validate("https://example.com:8080"));
    }

    @Test
    void rejectsLocalhostPrivateIpAndCloudMetadataAddresses() {
        assertThrows(SafeURLValidator.InvalidUrlException.class, () -> validator.validate("http://localhost"));
        assertThrows(SafeURLValidator.InvalidUrlException.class, () -> validator.validate("http://127.0.0.1"));
        assertThrows(SafeURLValidator.InvalidUrlException.class, () -> validator.validate("http://10.1.2.3"));
        assertThrows(SafeURLValidator.InvalidUrlException.class, () -> validator.validate("http://169.254.169.254"));
    }

    @Test
    void rejectsPrivateIpv6AddressesAndCredentials() {
        assertThrows(SafeURLValidator.InvalidUrlException.class, () -> validator.validate("http://[::1]/"));
        assertThrows(SafeURLValidator.InvalidUrlException.class, () -> validator.validate("http://[fc00::1]/"));
        assertThrows(
                SafeURLValidator.InvalidUrlException.class,
                () -> validator.validate("https://user:password@example.com"));
    }

    @Test
    void doesNotPerformDnsResolutionForDomainNames() {
        assertEquals(
                "nonexistent-domain.example",
                validator.validate("https://nonexistent-domain.example").normalizedDomain());
    }
}
