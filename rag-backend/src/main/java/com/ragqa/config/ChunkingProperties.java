package com.ragqa.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rag.chunking")
@Getter
@Setter
public class ChunkingProperties {

    private int chunkSizeTokens = 500;
    private int chunkOverlapTokens = 50;
}
