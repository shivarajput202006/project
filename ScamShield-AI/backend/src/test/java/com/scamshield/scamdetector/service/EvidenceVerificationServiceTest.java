package com.scamshield.scamdetector.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class EvidenceVerificationServiceTest {
    @Test
    void rejectsLoopbackAndPrivateAddressesBeforeAnalysis() {
        EvidenceVerificationService verifier = new EvidenceVerificationService(
                host -> List.of(InetAddress.getByName("8.8.8.8")));

        assertThrows(
                EvidenceVerificationService.UnsafeUrlException.class,
                () -> verifier.verifyUrl("http://127.0.0.1/apply"));
        assertThrows(
                EvidenceVerificationService.UnsafeUrlException.class,
                () -> verifier.verifyUrl("http://localhost/apply"));
        assertThrows(
                EvidenceVerificationService.UnsafeUrlException.class,
                () -> verifier.verifyUrl("https://192.168.1.5/apply"));
    }

    @Test
    void rejectsAHostnameThatResolvesToAPrivateAddress() {
        EvidenceVerificationService verifier = new EvidenceVerificationService(
                host -> List.of(InetAddress.getByName("10.2.3.4")));

        assertThrows(
                EvidenceVerificationService.UnsafeUrlException.class,
                () -> verifier.verifyUrl("https://jobs.example/apply"));
    }

    @Test
    void acceptsOnlyWebSchemesAndStandardPortsWithoutFetchingTheUrl() throws Exception {
        AtomicInteger resolutions = new AtomicInteger();
        EvidenceVerificationService verifier = new EvidenceVerificationService(host -> {
            resolutions.incrementAndGet();
            return List.of(InetAddress.getByName("8.8.8.8"));
        });

        EvidenceVerificationService.UrlVerification result =
                verifier.verifyUrl("https://jobs.example/apply");

        assertTrue(result.dnsResolved());
        assertFalse(result.externallyVerified());
        assertEquals(1, resolutions.get());
        assertThrows(
                EvidenceVerificationService.UnsafeUrlException.class,
                () -> verifier.verifyUrl("http://jobs.example:8080/apply"));
        assertThrows(
                EvidenceVerificationService.UnsafeUrlException.class,
                () -> verifier.verifyUrl("file:///etc/passwd"));
    }

    @Test
    void marksFailedDnsLookupsUnverifiedInsteadOfCallingTheUrl() {
        EvidenceVerificationService verifier = new EvidenceVerificationService(host -> {
            throw new UnknownHostException("test failure");
        });

        EvidenceVerificationService.UrlVerification result =
                verifier.verifyUrl("https://missing.example/apply");

        assertFalse(result.dnsResolved());
        assertFalse(result.externallyVerified());
        assertFalse(result.limitations().isEmpty());
    }
}
