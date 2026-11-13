package com.ragqa.service;

import com.ragqa.chunking.ChunkCandidate;
import com.ragqa.chunking.SlidingWindowChunker;
import com.ragqa.embedding.ChunkVectorDao;
import com.ragqa.embedding.EmbeddingService;
import com.ragqa.exception.DocumentNotFoundException;
import com.ragqa.extraction.PageText;
import com.ragqa.extraction.TextExtractionService;
import com.ragqa.model.Chunk;
import com.ragqa.model.Document;
import com.ragqa.repository.ChunkRepository;
import com.ragqa.repository.DocumentRepository;
import com.ragqa.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionPipeline implements IngestionPipeline {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final TextExtractionService textExtractionService;
    private final SlidingWindowChunker chunker;
    private final ChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;
    private final ChunkVectorDao chunkVectorDao;

    @Override
    public void process(UUID documentId) throws Exception {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));

        String extension = extractExtension(document.getFilename());

        List<PageText> pages;
        try (InputStream in = fileStorageService.load(document.getStorageKey())) {
            pages = textExtractionService.extract(extension, in);
        }

        List<ChunkCandidate> candidates = chunker.chunk(pages);
        log.info("Document {} produced {} chunks", documentId, candidates.size());

        List<Chunk> chunks = candidates.stream().map(c -> {
            Chunk chunk = new Chunk();
            chunk.setDocumentId(documentId);
            chunk.setChunkText(c.text());
            chunk.setChunkIndex(c.chunkIndex());
            chunk.setPageNumber(c.pageNumber());
            return chunk;
        }).toList();

        chunks = chunkRepository.saveAll(chunks);

        if (chunks.isEmpty()) {
            log.warn("Document {} produced zero chunks (empty extracted text).", documentId);
            return;
        }

        List<String> texts = chunks.stream().map(Chunk::getChunkText).toList();
        List<float[]> vectors = embeddingService.embedAll(texts);

        Map<UUID, float[]> embeddingsByChunkId = new HashMap<>();
        for (int i = 0; i < chunks.size(); i++) {
            embeddingsByChunkId.put(chunks.get(i).getId(), vectors.get(i));
        }
        chunkVectorDao.batchUpdateEmbeddings(embeddingsByChunkId);
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
