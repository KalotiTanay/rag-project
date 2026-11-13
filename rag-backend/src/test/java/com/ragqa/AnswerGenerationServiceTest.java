package com.ragqa;

import com.ragqa.dto.QueryResponse;
import com.ragqa.generation.AnswerGenerationService;
import com.ragqa.generation.CitationParser;
import com.ragqa.generation.LlmClient;
import com.ragqa.generation.PromptBuilder;
import com.ragqa.model.Conversation;
import com.ragqa.model.MessageRole;
import com.ragqa.retrieval.RetrievalService;
import com.ragqa.retrieval.RetrievedChunk;
import com.ragqa.service.ConversationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnswerGenerationServiceTest {

    @Mock private RetrievalService retrievalService;
    @Mock private LlmClient llmClient;
    @Mock private ConversationService conversationService;

    private final PromptBuilder promptBuilder = new PromptBuilder();
    private final CitationParser citationParser = new CitationParser();

    private AnswerGenerationService newService() {
        return new AnswerGenerationService(retrievalService, promptBuilder, llmClient, citationParser, conversationService);
    }

    @Test
    void noRetrievedChunks_returnsGuardrailMessage_andNeverCallsLlm() {
        AnswerGenerationService service = newService();
        Conversation conversation = new Conversation();
        conversation.setId(UUID.randomUUID());

        when(conversationService.resolveOrCreate(null)).thenReturn(conversation);
        when(retrievalService.retrieve("unrelated question")).thenReturn(List.of());

        QueryResponse response = service.generateAnswer(null, "unrelated question");

        assertThat(response.answer()).contains("don't have information");
        assertThat(response.citations()).isEmpty();
        verify(llmClient, never()).generate(any(), any());
    }

    @Test
    void withRetrievedChunks_callsLlmAndAttachesCitations() {
        AnswerGenerationService service = newService();
        Conversation conversation = new Conversation();
        conversation.setId(UUID.randomUUID());

        RetrievedChunk chunk = new RetrievedChunk(
                UUID.randomUUID(), UUID.randomUUID(), "policy.pdf", "Refunds within 30 days.", 4, 0.9);

        when(conversationService.resolveOrCreate(null)).thenReturn(conversation);
        when(retrievalService.retrieve("refund policy?")).thenReturn(List.of(chunk));
        when(llmClient.generate(any(), any())).thenReturn("Refunds are allowed within 30 days [Source 1].");

        QueryResponse response = service.generateAnswer(null, "refund policy?");

        assertThat(response.answer()).contains("30 days");
        assertThat(response.citations()).hasSize(1);
        assertThat(response.citations().get(0).documentFilename()).isEqualTo("policy.pdf");

        verify(conversationService).appendMessage(eq(conversation.getId()), eq(MessageRole.user), eq("refund policy?"), eq(null));
        verify(conversationService).appendMessage(eq(conversation.getId()), eq(MessageRole.assistant), any(), any(UUID[].class));
    }
}
