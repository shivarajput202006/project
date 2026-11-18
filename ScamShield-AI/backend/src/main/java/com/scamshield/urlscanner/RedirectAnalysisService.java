package com.scamshield.urlscanner;

import org.springframework.stereotype.Service;

@Service
public class RedirectAnalysisService {
    public URLScanResponse.RedirectSummary summarize() {
        return new URLScanResponse.RedirectSummary(
                "NOT_CHECKED",
                null,
                "The submitted URL was never requested, so its redirect chain is unknown.");
    }
}
