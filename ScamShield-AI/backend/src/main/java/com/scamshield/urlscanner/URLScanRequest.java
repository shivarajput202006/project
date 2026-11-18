package com.scamshield.urlscanner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record URLScanRequest(
        @NotBlank(message = "Enter an HTTP or HTTPS URL.")
        @Size(max = 2048, message = "URLs must be 2048 characters or fewer.")
        String url) {
}
