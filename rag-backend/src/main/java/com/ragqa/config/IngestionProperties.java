package com.ragqa.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "rag.ingestion")
@Getter
@Setter
public class IngestionProperties {

    private int maxFileSizeMb = 25;

    private List<String> allowedFileTypes;
}
