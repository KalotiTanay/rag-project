package com.ragqa.retrieval;

import java.util.UUID;

public record RetrievedChunk(
        UUID chunkId,
        UUID documentId,
        String documentFilename,
        String chunkText,
        Integer pageNumber,
        double similarityScore
) {
}
