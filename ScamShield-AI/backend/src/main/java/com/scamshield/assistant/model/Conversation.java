package com.scamshield.assistant.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "assistant_conversations")
@CompoundIndex(name = "assistant_owner_updated_idx", def = "{'userId': 1, 'updatedAt': -1}")
public class Conversation {
    @Id
    private String id;
    private String userId;
    private String title;
    private List<ChatMessage> messages;
    private Instant createdAt;
    private Instant updatedAt;

    protected Conversation() {
    }

    public Conversation(String userId, String title, Instant createdAt) {
        this.userId = userId;
        this.title = title;
        this.messages = new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public void append(ChatMessage userMessage, ChatMessage assistantMessage, Instant updatedAt) {
        if (messages == null) messages = new ArrayList<>();
        messages.add(userMessage);
        messages.add(assistantMessage);
        while (messages.size() > 80) messages.remove(0);
        this.updatedAt = updatedAt;
    }

    public boolean addFeedback(String messageId, com.scamshield.assistant.enums.FeedbackType feedback, Instant at) {
        if (messages == null) return false;
        return messages.stream()
                .filter(message -> message.getId().equals(messageId))
                .filter(message -> message.getRole() == com.scamshield.assistant.enums.MessageRole.ASSISTANT)
                .findFirst()
                .map(message -> {
                    message.addFeedback(feedback, at);
                    updatedAt = at;
                    return true;
                })
                .orElse(false);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public List<ChatMessage> getMessages() { return messages == null ? List.of() : List.copyOf(messages); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
