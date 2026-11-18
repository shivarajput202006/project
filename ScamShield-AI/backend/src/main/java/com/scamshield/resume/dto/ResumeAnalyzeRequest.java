package com.scamshield.resume.dto;

import jakarta.validation.constraints.AssertTrue;

public record ResumeAnalyzeRequest(
        @AssertTrue(message = "Consent is required before sending resume text to the configured AI service.")
        boolean consentToAiProcessing) {
}
