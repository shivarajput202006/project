package com.scamshield.assistant.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.scamshield.assistant.dto.ChatRequest;
import com.scamshield.assistant.dto.ChatResponse;
import com.scamshield.assistant.dto.ConversationResponse;
import com.scamshield.assistant.dto.FeedbackRequest;
import com.scamshield.assistant.service.AssistantFeedbackService;
import com.scamshield.assistant.service.ConversationService;
import com.scamshield.security.UserPrincipal;

@Validated
@RestController
@RequestMapping("/api/assistant")
public class AssistantController {
    private final ConversationService conversationService;
    private final AssistantFeedbackService feedbackService;

    public AssistantController(
            ConversationService conversationService,
            AssistantFeedbackService feedbackService) {
        this.conversationService = conversationService;
        this.feedbackService = feedbackService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChatRequest request) {
        boolean isNewConversation = request.conversationId() == null || request.conversationId().isBlank();
        ChatResponse response = conversationService.chat(principal.getUser().getId(), request);
        return ResponseEntity.status(isNewConversation ? HttpStatus.CREATED : HttpStatus.OK).body(response);
    }

    @GetMapping("/conversations")
    public Page<ConversationResponse> conversations(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") @Min(0) @Max(10000) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return conversationService.list(principal.getUser().getId(), page, size);
    }

    @GetMapping("/conversations/{id}")
    public ConversationResponse conversation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        return conversationService.get(principal.getUser().getId(), id);
    }

    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<Void> deleteConversation(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id) {
        conversationService.delete(principal.getUser().getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/conversations/{id}/feedback")
    public ResponseEntity<Void> feedback(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String id,
            @Valid @RequestBody FeedbackRequest request) {
        feedbackService.addFeedback(principal.getUser().getId(), id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/conversations")
    public ResponseEntity<Void> clearConversations(@AuthenticationPrincipal UserPrincipal principal) {
        conversationService.clear(principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
