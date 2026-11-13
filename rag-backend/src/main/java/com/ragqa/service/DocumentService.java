package com.ragqa.service;

import com.ragqa.exception.DocumentNotFoundException;
import com.ragqa.model.Document;
import com.ragqa.model.DocumentStatus;
import com.ragqa.repository.DocumentRepository;
import com.ragqa.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

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

        Document document = new Document();
        document.setFilename(file.getOriginalFilename());
        document.setOwnerId(DEFAULT_OWNER);
        document.setStatus(DocumentStatus.PROCESSING);

        document = documentRepository.save(document);

        String storageKey = fileStorageService.store(file, document.getId());
        document.setStorageKey(storageKey);
        document = documentRepository.save(document);

        ingestionOrchestrator.processDocumentAsync(document.getId());
        return document;
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
