package com.scamshield.urlscanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class RedirectAnalysisServiceTest {
    @Test
    void explicitlyDoesNotFollowOrClaimToKnowRedirectChains() {
        URLScanResponse.RedirectSummary summary = new RedirectAnalysisService().summarize();

        assertEquals("NOT_CHECKED", summary.status());
        assertNull(summary.redirectCount());
    }

    @Test
    void redirectToPrivateIpIsNeverRequestedOrReportedAsInspected() {
        URLScanResponse.RedirectSummary summary = new RedirectAnalysisService().summarize();

        assertEquals("NOT_CHECKED", summary.status());
        assertEquals("The submitted URL was never requested, so its redirect chain is unknown.", summary.detail());
    }

    @Test
    void anExcessiveRedirectChainCannotBeFollowedByThisScanner() {
        URLScanResponse.RedirectSummary summary = new RedirectAnalysisService().summarize();

        assertEquals("NOT_CHECKED", summary.status());
        assertNull(summary.redirectCount());
    }
}
