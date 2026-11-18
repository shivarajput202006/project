package com.scamshield.assistant.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.scamshield.assistant.enums.ContextSource;

public record ChatRequest(
        @Size(max = 100) String conversationId,
        @NotBlank @Size(max = 3000) String message,
        @Size(max = 4) List<ContextSource> contextSources) {
}
