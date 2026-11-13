package com.ragqa.generation;

public interface LlmClient {

    String generate(String systemPrompt, String userPrompt);
}
