package com.ragqa.embedding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenAiEmbeddingResponse(List<EmbeddingData> data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EmbeddingData(int index, float[] embedding) {
    }
}
