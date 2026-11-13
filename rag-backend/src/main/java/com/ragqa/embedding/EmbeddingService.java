package com.ragqa.embedding;

import com.ragqa.config.EmbeddingProperties;
import com.ragqa.exception.EmbeddingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingClient embeddingClient;
    private final EmbeddingProperties embeddingProperties;

    public List<float[]> embedAll(List<String> texts) {
        List<float[]> results = new ArrayList<>(texts.size());
        int batchSize = embeddingProperties.getBatchSize();
        long startedAt = System.currentTimeMillis();

        for (int start = 0; start < texts.size(); start += batchSize) {
            int end = Math.min(start + batchSize, texts.size());
            List<String> batch = texts.subList(start, end);

            try {
                List<float[]> batchVectors = embeddingClient.embedBatch(batch);
                results.addAll(batchVectors);
            } catch (Exception e) {
                throw new EmbeddingException(
                        "Embedding failed on batch covering chunks [" + start + ", " + end + "): " + e.getMessage(), e);
            }
        }

        long elapsedMs = System.currentTimeMillis() - startedAt;
        log.info("Embedded {} chunks in {} batch(es), {}ms ({} chunks/sec)",
                texts.size(),
                (int) Math.ceil((double) texts.size() / batchSize),
                elapsedMs,
                elapsedMs == 0 ? "n/a" : String.format("%.1f", texts.size() * 1000.0 / elapsedMs));

        return results;
    }
}
