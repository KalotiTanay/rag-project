package com.ragqa.dto;

import com.ragqa.model.Conversation;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConversationSummary(
        UUID id,
        OffsetDateTime createdAt
) {
    public static ConversationSummary fromEntity(Conversation conversation) {
        return new ConversationSummary(conversation.getId(), conversation.getCreatedAt());
    }
}
