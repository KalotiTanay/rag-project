package com.ragqa.dto;

import com.ragqa.model.Message;
import com.ragqa.model.MessageRole;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        MessageRole role,
        String content,
        List<UUID> citedChunkIds,
        OffsetDateTime createdAt
) {
    public static MessageResponse fromEntity(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                List.of(message.getCitedChunkIds()),
                message.getCreatedAt()
        );
    }
}
