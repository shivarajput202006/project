package com.scamshield.assistant.dto;

import java.time.Instant;

public record ChatResponse(
        String conversationId,
        String title,
        MessageResponse userMessage,
        MessageResponse assistantMessage,
        Instant updatedAt) {
}
