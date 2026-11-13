package com.ragqa.generation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ragqa.config.LlmProperties;
import com.ragqa.exception.LlmException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Component
public class AnthropicStreamingLlmClient implements StreamingLlmClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LlmProperties llmProperties;
    private final String apiKey;

    public AnthropicStreamingLlmClient(LlmProperties llmProperties,
                                        @Value("${ANTHROPIC_API_KEY:}") String apiKey) {
        this.llmProperties = llmProperties;
        this.apiKey = apiKey;
    }

    @Override
    public void generateStream(String systemPrompt, String userPrompt, TokenHandler handler) {
        if (apiKey == null || apiKey.isBlank()) {
            handler.onError(new LlmException("ANTHROPIC_API_KEY is not set."));
            return;
        }

        try {
            String bodyJson = objectMapper.writeValueAsString(Map.of(
                    "model", llmProperties.getModel(),
                    "max_tokens", llmProperties.getMaxTokens(),
                    "system", systemPrompt,
                    "stream", true,
                    "messages", List.of(Map.of("role", "user", "content", userPrompt))
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(llmProperties.getBaseUrl() + "/messages"))
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", llmProperties.getAnthropicVersion())
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                    .build();

            HttpResponse<Stream<String>> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofLines());

            if (response.statusCode() >= 400) {
                handler.onError(new LlmException("LLM streaming API returned status " + response.statusCode()));
                return;
            }

            response.body().forEach(line -> processLine(line, handler));
            handler.onComplete();

        } catch (Exception e) {
            handler.onError(new LlmException("LLM streaming call failed: " + e.getMessage(), e));
        }
    }

    private void processLine(String line, TokenHandler handler) {
        if (line == null || !line.startsWith("data: ")) {
            return;
        }
        String json = line.substring("data: ".length()).trim();
        if (json.isEmpty() || json.equals("[DONE]")) {
            return;
        }

        try {
            AnthropicStreamEvent event = objectMapper.readValue(json, AnthropicStreamEvent.class);
            if ("content_block_delta".equals(event.type()) && event.delta() != null
                    && "text_delta".equals(event.delta().type())) {
                handler.onToken(event.delta().text());
            }

        } catch (Exception e) {
            log.warn("Failed to parse SSE line from Anthropic stream, skipping: {}", json, e);
        }
    }
}
