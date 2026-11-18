package com.scamshield.assistant.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.scamshield.assistant.dto.ChatRequest;
import com.scamshield.assistant.dto.ChatResponse;
import com.scamshield.assistant.dto.ConversationResponse;
import com.scamshield.assistant.dto.MessageResponse;
import com.scamshield.assistant.enums.ContextSource;
import com.scamshield.assistant.enums.MessageRole;
import com.scamshield.assistant.exception.AssistantException;
import com.scamshield.assistant.model.ChatMessage;
import com.scamshield.assistant.model.Conversation;
import com.scamshield.assistant.repository.ConversationRepository;
import com.scamshield.profile.service.PrivacyService;

@Service
public class ConversationService {
    private static final int MAX_REQUEST_CHARS = 3000;
    private final ConversationRepository conversationRepository;
    private final GeminiAssistantService geminiAssistantService;
    private final AssistantContextService contextService;
    private final AssistantRateLimiter rateLimiter;
    private final PrivacyService privacyService;

    public ConversationService(
            ConversationRepository conversationRepository,
            GeminiAssistantService geminiAssistantService,
            AssistantContextService contextService,
            AssistantRateLimiter rateLimiter,
            PrivacyService privacyService) {
        this.conversationRepository = conversationRepository;
        this.geminiAssistantService = geminiAssistantService;
        this.contextService = contextService;
        this.rateLimiter = rateLimiter;
        this.privacyService = privacyService;
    }

    public ChatResponse chat(String userId, ChatRequest request) {
        if (userId == null || userId.isBlank()) throw AssistantException.invalid("Authenticated user is unavailable.");
        String messageText = request.message().trim();
        if (messageText.isEmpty() || messageText.length() > MAX_REQUEST_CHARS) {
            throw AssistantException.invalid("Message must contain 1 to 3000 characters.");
        }
        privacyService.requireAiProcessingEnabled(userId);
        rateLimiter.check(userId);

        Conversation conversation = null;
        if (request.conversationId() != null && !request.conversationId().isBlank()) {
            conversation = conversationRepository.findByIdAndUserId(request.conversationId(), userId)
                    .orElseThrow(AssistantException::notFound);
        }

        String context = contextService.buildContext(userId, request.contextSources());
        var history = conversation == null ? java.util.List.<ChatMessage>of() : conversation.getMessages();
        String answer = geminiAssistantService.generate(history, messageText, context);
        Instant now = Instant.now();
        ChatMessage userMessage = new ChatMessage(UUID.randomUUID().toString(), MessageRole.USER, messageText, now);
        ChatMessage assistantMessage =
                new ChatMessage(UUID.randomUUID().toString(), MessageRole.ASSISTANT, answer, now);

        if (conversation == null) {
            conversation = new Conversation(userId, createTitle(messageText), now);
        }
        conversation.append(userMessage, assistantMessage, now);
        Conversation saved = conversationRepository.save(conversation);
        return new ChatResponse(
                saved.getId(),
                saved.getTitle(),
                MessageResponse.from(userMessage),
                MessageResponse.from(assistantMessage),
                saved.getUpdatedAt());
    }

    public Page<ConversationResponse> list(String userId, int page, int size) {
        return conversationRepository.findByUserId(
                        userId,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt")))
                .map(ConversationResponse::summary);
    }

    public ConversationResponse get(String userId, String id) {
        return ConversationResponse.detail(findOwned(userId, id));
    }

    public void delete(String userId, String id) {
        if (conversationRepository.deleteByIdAndUserId(id, userId) == 0) {
            throw AssistantException.notFound();
        }
    }

    public void clear(String userId) {
        conversationRepository.deleteByUserId(userId);
    }

    private Conversation findOwned(String userId, String id) {
        return conversationRepository.findByIdAndUserId(id, userId).orElseThrow(AssistantException::notFound);
    }

    private String createTitle(String message) {
        String title = message.replaceAll("\\s+", " ").trim();
        return title.length() <= 72 ? title : title.substring(0, 69) + "...";
    }
}
