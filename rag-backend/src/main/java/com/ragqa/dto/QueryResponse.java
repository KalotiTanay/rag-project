package com.ragqa.dto;

import java.util.List;
import java.util.UUID;

public record QueryResponse(
        UUID conversationId,
        String answer,
        List<Citation> citations
) {
}
