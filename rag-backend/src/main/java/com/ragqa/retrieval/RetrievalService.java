package com.ragqa.retrieval;

import com.ragqa.config.RetrievalProperties;
import com.ragqa.embedding.EmbeddingService;
import com.ragqa.model.RetrievalLog;
import com.ragqa.repository.RetrievalLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetrievalService {

    private final EmbeddingService embeddingService;
    private final VectorSearchDao vectorSearchDao;
    private final RetrievalProperties retrievalProperties;
    private final RetrievalLogRepository retrievalLogRepository;

    public List<RetrievedChunk> retrieve(String question) {
        return retrieve(question, null);
    }

    public List<RetrievedChunk> retrieve(String question, UUID conversationId) {
        float[] queryVector = embeddingService.embedAll(List.of(question)).get(0);

        List<RetrievedChunk> candidates = vectorSearchDao.searchTopK(queryVector, retrievalProperties.getTopK());

        List<RetrievedChunk> aboveThreshold = candidates.stream()
                .filter(c -> c.similarityScore() >= retrievalProperties.getSimilarityThreshold())
                .toList();

        log.info("Retrieval for question (len={}): {} candidates, {} above threshold {}",
                question.length(), candidates.size(), aboveThreshold.size(),
                retrievalProperties.getSimilarityThreshold());

        logRetrievalQuality(question, conversationId, candidates, !aboveThreshold.isEmpty());

        return aboveThreshold;
    }

    private void logRetrievalQuality(String question, UUID conversationId,
                                      List<RetrievedChunk> candidates, boolean passedThreshold) {
        try {
            RetrievalLog logEntry = new RetrievalLog();
            logEntry.setConversationId(conversationId);
            logEntry.setQuestion(question);
            logEntry.setRetrievedChunkIds(candidates.stream().map(RetrievedChunk::chunkId).toArray(UUID[]::new));
            logEntry.setSimilarityScores(candidates.stream().map(RetrievedChunk::similarityScore).toArray(Double[]::new));
            logEntry.setThresholdUsed(retrievalProperties.getSimilarityThreshold());
            logEntry.setPassedThreshold(passedThreshold);
            retrievalLogRepository.save(logEntry);
        } catch (Exception e) {
            log.warn("Failed to persist retrieval quality log (non-fatal): {}", e.getMessage());
        }
    }
}
