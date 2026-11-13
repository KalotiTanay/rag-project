package com.ragqa.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rag.llm")
@Getter
@Setter
public class LlmProperties {

    private String model = "claude-sonnet-4-5";
    private String baseUrl = "https://api.anthropic.com/v1";
    private String anthropicVersion = "2023-06-01";
    private int maxTokens = 1024;
}
