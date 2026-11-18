package com.scamshield.assistant.dto;

import java.time.Instant;

import com.scamshield.assistant.enums.FeedbackType;
import com.scamshield.assistant.enums.MessageRole;
import com.scamshield.assistant.model.ChatMessage;

public record MessageResponse(
        String id,
        MessageRole role,
        String content,
        Instant createdAt,
        FeedbackType feedback) {
    public static MessageResponse from(ChatMessage message) {
        return new MessageResponse(
                message.getId(), message.getRole(), message.getContent(), message.getCreatedAt(), message.getFeedback());
    }
}
