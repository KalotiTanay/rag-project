package com.ragqa.service;

import com.ragqa.exception.DocumentNotFoundException;
import com.ragqa.model.Chunk;
import com.ragqa.repository.ChunkRepository;
import com.ragqa.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChunkService {

    private final ChunkRepository chunkRepository;
    private final DocumentRepository documentRepository;

    @Transactional(readOnly = true)
    public List<Chunk> getChunksForDocument(UUID documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new DocumentNotFoundException(documentId);
        }
        return chunkRepository.findAllByDocumentIdOrderByChunkIndexAsc(documentId);
    }
}
