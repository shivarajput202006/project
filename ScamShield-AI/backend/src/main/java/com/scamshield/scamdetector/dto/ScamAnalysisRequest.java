package com.scamshield.scamdetector.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ScamAnalysisRequest {
    private ScamAnalysisRequest() {
    }

    public record Text(
            @NotBlank(message = "Paste the message you want to review.")
            @Size(max = 20000, message = "Text must be 20,000 characters or fewer.")
            String text) {
    }

    public record EmailMessage(
            @NotBlank(message = "Enter the email subject.")
            @Size(max = 300, message = "Subject must be 300 characters or fewer.")
            String subject,
            @NotBlank(message = "Enter the sender email address.")
            @Email(message = "Enter a valid sender email address.")
            @Size(max = 254, message = "Email address is too long.")
            String senderEmail,
            @NotBlank(message = "Paste the email body.")
            @Size(max = 20000, message = "Email body must be 20,000 characters or fewer.")
            String body) {
    }

    public record Url(
            @NotBlank(message = "Enter the URL you want to review.")
            @Size(max = 2048, message = "URL must be 2,048 characters or fewer.")
            @Pattern(regexp = "(?i)^https?://.+", message = "Only HTTP and HTTPS URLs are accepted.")
            String url) {
    }
}
