package com.ragqa.generation;

public interface StreamingLlmClient {
    void generateStream(String systemPrompt, String userPrompt, TokenHandler handler);
}
