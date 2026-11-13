package com.ragqa.dto;

import com.ragqa.retrieval.RetrievedChunk;

import java.util.UUID;

public record Citation(
        UUID chunkId,
        String documentFilename,
        Integer pageNumber
) {
    public static Citation fromRetrievedChunk(RetrievedChunk chunk) {
        return new Citation(chunk.chunkId(), chunk.documentFilename(), chunk.pageNumber());
    }
}
