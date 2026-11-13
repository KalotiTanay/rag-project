package com.ragqa.generation;

import com.ragqa.dto.Citation;
import com.ragqa.dto.QueryResponse;
import com.ragqa.model.Conversation;
import com.ragqa.model.MessageRole;
import com.ragqa.retrieval.RetrievalService;
import com.ragqa.retrieval.RetrievedChunk;
import com.ragqa.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerGenerationService {

    private static final String NO_RELEVANT_INFO_MESSAGE =
            "I don't have information on that in the uploaded documents.";

    private final RetrievalService retrievalService;
    private final PromptBuilder promptBuilder;
    private final LlmClient llmClient;
    private final CitationParser citationParser;
    private final ConversationService conversationService;

    public QueryResponse generateAnswer(UUID conversationId, String question) {
        Conversation conversation = conversationService.resolveOrCreate(conversationId);
        MDC.put("conversationId", conversation.getId().toString());
        try {
            return doGenerateAnswer(conversation, question);
        } finally {
            MDC.remove("conversationId");
        }
    }

    private QueryResponse doGenerateAnswer(Conversation conversation, String question) {

        conversationService.appendMessage(conversation.getId(), MessageRole.user, question, null);

        List<RetrievedChunk> retrievedChunks = retrievalService.retrieve(question);

        String answer;
        List<Citation> citations;
        UUID[] citedChunkIds;

        if (retrievedChunks.isEmpty()) {

            answer = NO_RELEVANT_INFO_MESSAGE;
            citations = List.of();
            citedChunkIds = new UUID[0];
        } else {
            String systemPrompt = promptBuilder.buildSystemPrompt();
            String userPrompt = promptBuilder.buildUserPrompt(question, retrievedChunks);

            answer = llmClient.generate(systemPrompt, userPrompt);

            List<RetrievedChunk> citedChunks = citationParser.extractCitedChunks(answer, retrievedChunks);
            citations = citedChunks.stream().map(Citation::fromRetrievedChunk).toList();
            citedChunkIds = citedChunks.stream().map(RetrievedChunk::chunkId).toArray(UUID[]::new);
        }

        conversationService.appendMessage(conversation.getId(), MessageRole.assistant, answer, citedChunkIds);

        return new QueryResponse(conversation.getId(), answer, citations);
    }
}
