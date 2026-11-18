package com.scamshield.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.scamshield.assistant.enums.FeedbackType;

public record FeedbackRequest(
        @NotBlank @Size(max = 64) String messageId,
        @NotNull FeedbackType feedback) {
}
