package com.ragqa.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.ragqa.exception.DocumentNotFoundException;
import com.ragqa.model.Document;
import com.ragqa.model.DocumentStatus;
import com.ragqa.repository.DocumentRepository;
import com.ragqa.storage.FileStorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final FileStorageService fileStorageService;
    private final FileValidator fileValidator;
    private final IngestionOrchestrator ingestionOrchestrator;

    private static final String DEFAULT_OWNER = "default-user";

    @Transactional
    public Document uploadDocument(MultipartFile file) {
        fileValidator.validate(file);

        String storageKey = fileStorageService.store(file, UUID.randomUUID());

        Document saved;
        try {
            Document document = new Document();
            document.setFilename(file.getOriginalFilename());
            document.setStorageKey(storageKey);
            document.setOwnerId(DEFAULT_OWNER);
            document.setStatus(DocumentStatus.PROCESSING);
            saved = documentRepository.saveAndFlush(document);
        } catch (RuntimeException e) {
            fileStorageService.delete(storageKey);
            throw e;
        }

        UUID documentId = saved.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                ingestionOrchestrator.processDocumentAsync(documentId);
            }
        });
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Document> listDocuments() {
        return documentRepository.findAllByOwnerIdOrderByUploadDateDesc(DEFAULT_OWNER);
    }

    @Transactional(readOnly = true)
    public Document getDocument(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException(id));
    }

    @Transactional
    public void deleteDocument(UUID id) {
        Document document = getDocument(id);

        fileStorageService.delete(document.getStorageKey());

        documentRepository.delete(document);
    }
}
