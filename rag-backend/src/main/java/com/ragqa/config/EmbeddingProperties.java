package com.ragqa.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rag.embedding")
@Getter
@Setter
public class EmbeddingProperties {

    private String model = "text-embedding-3-small";
    private String baseUrl = "https://api.openai.com/v1";
    private int batchSize = 100;
    private int dimensions = 1536;
}
