package com.ragqa.generation;

import com.ragqa.config.LlmProperties;
import com.ragqa.exception.LlmException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AnthropicLlmClient implements LlmClient {

    private final RestClient restClient;
    private final LlmProperties llmProperties;
    private final String apiKey;

    public AnthropicLlmClient(LlmProperties llmProperties,
                               @Value("${ANTHROPIC_API_KEY:}") String apiKey) {
        this.llmProperties = llmProperties;
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(llmProperties.getBaseUrl())
                .build();
    }

    @Override
    @Retryable(
            retryFor = {LlmException.class, ResourceAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2)
    )
    public String generate(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new LlmException(
                    "ANTHROPIC_API_KEY is not set. Export it before starting the app, e.g. " +
                            "export ANTHROPIC_API_KEY=sk-ant-...");
        }

        try {
            AnthropicMessageResponse response = restClient.post()
                    .uri("/messages")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", llmProperties.getAnthropicVersion())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "model", llmProperties.getModel(),
                            "max_tokens", llmProperties.getMaxTokens(),
                            "system", systemPrompt,
                            "messages", List.of(Map.of("role", "user", "content", userPrompt))
                    ))
                    .retrieve()
                    .body(AnthropicMessageResponse.class);

            if (response == null || response.content() == null || response.content().isEmpty()) {
                throw new LlmException("LLM API returned an empty response.");
            }

            return response.content().stream()
                    .filter(block -> "text".equals(block.type()))
                    .map(AnthropicMessageResponse.ContentBlock::text)
                    .reduce("", (a, b) -> a + b);

        } catch (LlmException e) {
            throw e;
        } catch (Exception e) {
            throw new LlmException("LLM API call failed: " + e.getMessage(), e);
        }
    }
}
