package com.ragqa.generation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnthropicStreamEvent(String type, Delta delta) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Delta(String type, String text) {
    }
}
