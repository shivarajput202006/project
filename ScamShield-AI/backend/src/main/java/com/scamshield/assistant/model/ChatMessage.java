package com.scamshield.assistant.model;

import java.time.Instant;

import com.scamshield.assistant.enums.FeedbackType;
import com.scamshield.assistant.enums.MessageRole;

public class ChatMessage {
    private String id;
    private MessageRole role;
    private String content;
    private Instant createdAt;
    private FeedbackType feedback;
    private Instant feedbackAt;

    protected ChatMessage() {
    }

    public ChatMessage(String id, MessageRole role, String content, Instant createdAt) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    public void addFeedback(FeedbackType feedback, Instant feedbackAt) {
        this.feedback = feedback;
        this.feedbackAt = feedbackAt;
    }

    public String getId() { return id; }
    public MessageRole getRole() { return role; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
    public FeedbackType getFeedback() { return feedback; }
    public Instant getFeedbackAt() { return feedbackAt; }
}
