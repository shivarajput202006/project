package com.scamshield.assistant.dto;

import java.time.Instant;
import java.util.List;

import com.scamshield.assistant.model.Conversation;

public record ConversationResponse(
        String id,
        String title,
        Instant createdAt,
        Instant updatedAt,
        int messageCount,
        String lastMessagePreview,
        List<MessageResponse> messages) {
    public static ConversationResponse summary(Conversation conversation) {
        List<com.scamshield.assistant.model.ChatMessage> messages = conversation.getMessages();
        String preview = messages.isEmpty() ? "" : messages.get(messages.size() - 1).getContent();
        if (preview.length() > 140) preview = preview.substring(0, 137) + "...";
        return new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                messages.size(),
                preview,
                List.of());
    }

    public static ConversationResponse detail(Conversation conversation) {
        List<MessageResponse> messages = conversation.getMessages().stream().map(MessageResponse::from).toList();
        String preview = messages.isEmpty() ? "" : messages.get(messages.size() - 1).content();
        if (preview.length() > 140) preview = preview.substring(0, 137) + "...";
        return new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                messages.size(),
                preview,
                messages);
    }
}
