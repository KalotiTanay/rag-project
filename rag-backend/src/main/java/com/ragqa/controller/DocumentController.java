package com.ragqa.controller;

import com.ragqa.dto.ChunkResponse;
import com.ragqa.dto.DocumentResponse;
import com.ragqa.model.Document;
import com.ragqa.service.ChunkService;
import com.ragqa.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final ChunkService chunkService;

    @Value("${rag.debug.chunk-inspection-enabled:true}")
    private boolean chunkInspectionEnabled;

    @GetMapping
    public List<DocumentResponse> listDocuments() {
        return documentService.listDocuments().stream()
                .map(DocumentResponse::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public DocumentResponse getDocument(@PathVariable UUID id) {
        Document document = documentService.getDocument(id);
        return DocumentResponse.fromEntity(document);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<DocumentResponse> uploadDocument(@RequestParam("file") MultipartFile file) {
        Document document = documentService.uploadDocument(file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(DocumentResponse.fromEntity(document));
    }

    @GetMapping("/{id}/chunks")
    public ResponseEntity<List<ChunkResponse>> getChunks(@PathVariable UUID id) {
        if (!chunkInspectionEnabled) {
            return ResponseEntity.notFound().build();
        }
        List<ChunkResponse> chunks = chunkService.getChunksForDocument(id).stream()
                .map(ChunkResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(chunks);
    }
}
