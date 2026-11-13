package com.ragqa.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record QueryRequest(
        @NotBlank String question,
        UUID conversationId
) {
}
