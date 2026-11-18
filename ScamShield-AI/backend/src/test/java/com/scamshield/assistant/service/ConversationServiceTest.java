package com.scamshield.assistant.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.scamshield.assistant.dto.ChatRequest;
import com.scamshield.assistant.enums.ContextSource;
import com.scamshield.assistant.exception.AssistantException;
import com.scamshield.assistant.model.Conversation;
import com.scamshield.assistant.repository.ConversationRepository;
import com.scamshield.profile.service.PrivacyService;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {
    @Mock private ConversationRepository repository;
    @Mock private GeminiAssistantService gemini;
    @Mock private AssistantContextService context;
    @Mock private AssistantRateLimiter rateLimiter;
    @Mock private PrivacyService privacy;
    private ConversationService service;

    @BeforeEach
    void setUp() {
        service = new ConversationService(repository, gemini, context, rateLimiter, privacy);
    }

    @Test
    void persistsUserAndAssistantMessagesOnlyAfterSuccessfulGeneration() {
        when(context.buildContext("owner", List.of(ContextSource.PROFILE))).thenReturn("owned profile context");
        when(gemini.generate(List.of(), "How should I learn Java?", "owned profile context"))
                .thenReturn("Start with syntax, then build small programs.");
        when(repository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.chat("owner",
                new ChatRequest(null, "  How should I learn Java?  ", List.of(ContextSource.PROFILE)));

        assertEquals("How should I learn Java?", response.userMessage().content());
        ArgumentCaptor<Conversation> saved = ArgumentCaptor.forClass(Conversation.class);
        verify(repository).save(saved.capture());
        assertEquals(2, saved.getValue().getMessages().size());
    }

    @Test
    void doesNotPersistIfGeminiFails() {
        when(context.buildContext("owner", List.of())).thenReturn("");
        when(gemini.generate(List.of(), "Question", ""))
                .thenThrow(AssistantException.unavailable("AI unavailable"));

        assertThrows(AssistantException.class,
                () -> service.chat("owner", new ChatRequest(null, "Question", List.of())));

        verify(repository, never()).save(any(Conversation.class));
    }

    @Test
    void conversationMustBelongToAuthenticatedOwner() {
        when(repository.findByIdAndUserId("conversation-id", "owner")).thenReturn(Optional.empty());

        AssistantException exception = assertThrows(
                AssistantException.class,
                () -> service.chat("owner", new ChatRequest("conversation-id", "Question", List.of())));

        assertEquals(404, exception.getStatus().value());
        verify(gemini, never()).generate(any(), any(), any());
    }
}
