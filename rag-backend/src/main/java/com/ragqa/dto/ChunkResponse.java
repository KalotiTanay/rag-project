package com.ragqa.dto;

import com.ragqa.model.Chunk;

import java.util.UUID;

public record ChunkResponse(
        UUID id,
        Integer chunkIndex,
        Integer pageNumber,
        String chunkText
) {
    public static ChunkResponse fromEntity(Chunk chunk) {
        return new ChunkResponse(chunk.getId(), chunk.getChunkIndex(), chunk.getPageNumber(), chunk.getChunkText());
    }
}
