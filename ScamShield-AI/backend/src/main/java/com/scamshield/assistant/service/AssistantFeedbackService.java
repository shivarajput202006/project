package com.scamshield.assistant.service;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.scamshield.assistant.dto.FeedbackRequest;
import com.scamshield.assistant.exception.AssistantException;
import com.scamshield.assistant.repository.ConversationRepository;

@Service
public class AssistantFeedbackService {
    private final ConversationRepository conversationRepository;

    public AssistantFeedbackService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    public void addFeedback(String userId, String conversationId, FeedbackRequest request) {
        var conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(AssistantException::notFound);
        if (!conversation.addFeedback(request.messageId(), request.feedback(), Instant.now())) {
            throw AssistantException.invalid("Feedback can only be added to an assistant response in this conversation.");
        }
        conversationRepository.save(conversation);
    }
}
