package com.ragqa.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rag.retrieval")
@Getter
@Setter
public class RetrievalProperties {

    private int topK = 5;

    private double similarityThreshold = 0.75;
}
