package com.ragqa.generation;

import com.ragqa.dto.Citation;
import com.ragqa.dto.StreamDonePayload;
import com.ragqa.model.Conversation;
import com.ragqa.model.MessageRole;
import com.ragqa.retrieval.RetrievalService;
import com.ragqa.retrieval.RetrievedChunk;
import com.ragqa.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingAnswerService {

    private static final String NO_RELEVANT_INFO_MESSAGE =
            "I don't have information on that in the uploaded documents.";

    private final RetrievalService retrievalService;
    private final PromptBuilder promptBuilder;
    private final StreamingLlmClient streamingLlmClient;
    private final CitationParser citationParser;
    private final ConversationService conversationService;
    private final TaskExecutor sseTaskExecutor;

    public void streamAnswer(UUID conversationId, String question, SseEmitter emitter) {
        sseTaskExecutor.execute(() -> runStream(conversationId, question, emitter));
    }

    private void runStream(UUID conversationId, String question, SseEmitter emitter) {
        Conversation conversation;
        try {
            conversation = conversationService.resolveOrCreate(conversationId);
            conversationService.appendMessage(conversation.getId(), MessageRole.user, question, null);
        } catch (Exception e) {
            log.error("Failed to resolve conversation for streaming query", e);
            emitter.completeWithError(e);
            return;
        }

        UUID resolvedConversationId = conversation.getId();

        MDC.put("conversationId", resolvedConversationId.toString());
        try {
            streamAnswerInternal(resolvedConversationId, question, emitter);
        } finally {
            MDC.remove("conversationId");
        }
    }

    private void streamAnswerInternal(UUID resolvedConversationId, String question, SseEmitter emitter) {
        List<RetrievedChunk> retrievedChunks = retrievalService.retrieve(question);

        if (retrievedChunks.isEmpty()) {
            sendGuardrailAndComplete(emitter, resolvedConversationId);
            return;
        }

        String systemPrompt = promptBuilder.buildSystemPrompt();
        String userPrompt = promptBuilder.buildUserPrompt(question, retrievedChunks);

        StringBuilder fullAnswer = new StringBuilder();
        AtomicReference<Boolean> emitterClosed = new AtomicReference<>(false);

        streamingLlmClient.generateStream(systemPrompt, userPrompt, new TokenHandler() {
            @Override
            public void onToken(String tokenText) {
                fullAnswer.append(tokenText);
                trySend(emitter, "token", tokenText, emitterClosed);
            }

            @Override
            public void onComplete() {
                String answer = fullAnswer.toString();
                List<RetrievedChunk> citedChunks = citationParser.extractCitedChunks(answer, retrievedChunks);
                List<Citation> citations = citedChunks.stream().map(Citation::fromRetrievedChunk).toList();
                UUID[] citedChunkIds = citedChunks.stream().map(RetrievedChunk::chunkId).toArray(UUID[]::new);

                conversationService.appendMessage(resolvedConversationId, MessageRole.assistant, answer, citedChunkIds);

                trySend(emitter, "done", new StreamDonePayload(resolvedConversationId, citations), emitterClosed);
                if (!emitterClosed.get()) {
                    emitter.complete();
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("Streaming LLM call failed for conversation {}", resolvedConversationId, t);
                if (!emitterClosed.get()) {
                    emitter.completeWithError(t);
                }
            }
        });
    }

    private void sendGuardrailAndComplete(SseEmitter emitter, UUID conversationId) {
        conversationService.appendMessage(conversationId, MessageRole.assistant, NO_RELEVANT_INFO_MESSAGE, null);
        AtomicReference<Boolean> closed = new AtomicReference<>(false);
        trySend(emitter, "token", NO_RELEVANT_INFO_MESSAGE, closed);
        trySend(emitter, "done", new StreamDonePayload(conversationId, List.of()), closed);
        if (!closed.get()) {
            emitter.complete();
        }
    }

    private void trySend(SseEmitter emitter, String eventName, Object data, AtomicReference<Boolean> closedFlag) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (Exception e) {

            log.info("SSE client disconnected mid-stream: {}", e.getMessage());
            closedFlag.set(true);
        }
    }
}
