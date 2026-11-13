package com.ragqa.embedding;

import com.ragqa.config.EmbeddingProperties;
import com.ragqa.exception.EmbeddingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OpenAiEmbeddingClient implements EmbeddingClient {

    private final RestClient restClient;
    private final EmbeddingProperties embeddingProperties;
    private final String apiKey;

    public OpenAiEmbeddingClient(EmbeddingProperties embeddingProperties,
                                  @Value("${OPENAI_API_KEY:}") String apiKey) {
        this.embeddingProperties = embeddingProperties;
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(embeddingProperties.getBaseUrl())
                .build();
    }

    @Override
    @Retryable(
            retryFor = {EmbeddingException.class, org.springframework.web.client.ResourceAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2)
    )
    public List<float[]> embedBatch(List<String> texts) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new EmbeddingException(
                    "OPENAI_API_KEY is not set. Export it before starting the app, e.g. " +
                            "export OPENAI_API_KEY=sk-...");
        }
        if (texts.isEmpty()) {
            return List.of();
        }

        try {
            OpenAiEmbeddingResponse response = restClient.post()
                    .uri("/embeddings")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "model", embeddingProperties.getModel(),
                            "input", texts
                    ))
                    .retrieve()
                    .body(OpenAiEmbeddingResponse.class);

            if (response == null || response.data() == null || response.data().size() != texts.size()) {
                throw new EmbeddingException("Embedding API returned an unexpected number of vectors.");
            }

            return response.data().stream()
                    .sorted(Comparator.comparingInt(OpenAiEmbeddingResponse.EmbeddingData::index))
                    .map(OpenAiEmbeddingResponse.EmbeddingData::embedding)
                    .toList();

        } catch (EmbeddingException e) {
            throw e;
        } catch (Exception e) {
            throw new EmbeddingException("Embedding API call failed: " + e.getMessage(), e);
        }
    }
}
