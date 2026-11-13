package com.ragqa.dto;

import java.util.List;
import java.util.UUID;

public record StreamDonePayload(
        UUID conversationId,
        List<Citation> citations
) {
}
