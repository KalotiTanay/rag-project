package com.ragqa.service;

import com.ragqa.model.Document;
import com.ragqa.model.DocumentStatus;
import com.ragqa.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionOrchestrator {

    private final DocumentRepository documentRepository;
    private final IngestionPipeline ingestionPipeline;

    @Async("ingestionTaskExecutor")
    public void processDocumentAsync(UUID documentId) {

        MDC.put("documentId", documentId.toString());
        try {
            ingestionPipeline.process(documentId);
            markStatus(documentId, DocumentStatus.READY, null);
        } catch (Exception e) {
            log.error("Ingestion failed for document {}", documentId, e);
            markStatus(documentId, DocumentStatus.FAILED, truncate(e.getMessage()));
        } finally {
            MDC.remove("documentId");
        }
    }

    private void markStatus(UUID documentId, DocumentStatus status, String failureReason) {
        Document document = documentRepository.findById(documentId).orElse(null);
        if (document == null) {

            log.warn("Document {} no longer exists; skipping status update to {}", documentId, status);
            return;
        }
        document.setStatus(status);
        document.setFailureReason(failureReason);
        documentRepository.save(document);
    }

    private String truncate(String message) {
        if (message == null) return "Unknown error during ingestion";
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
