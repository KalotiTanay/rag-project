package com.ragqa;

import com.ragqa.exception.DocumentNotFoundException;
import com.ragqa.model.Document;
import com.ragqa.repository.DocumentRepository;
import com.ragqa.service.DocumentService;
import com.ragqa.service.FileValidator;
import com.ragqa.service.IngestionOrchestrator;
import com.ragqa.storage.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private FileValidator fileValidator;
    @Mock
    private IngestionOrchestrator ingestionOrchestrator;

    private DocumentService newService() {
        return new DocumentService(documentRepository, fileStorageService, fileValidator, ingestionOrchestrator);
    }

    @Test
    void getDocument_returnsDocument_whenFound() {
        DocumentService service = newService();
        UUID id = UUID.randomUUID();
        Document doc = new Document();
        doc.setId(id);
        doc.setFilename("test.pdf");

        when(documentRepository.findById(id)).thenReturn(Optional.of(doc));

        Document result = service.getDocument(id);

        assertThat(result.getFilename()).isEqualTo("test.pdf");
    }

    @Test
    void getDocument_throws_whenNotFound() {
        DocumentService service = newService();
        UUID id = UUID.randomUUID();

        when(documentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDocument(id))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    void deleteDocument_deletesStoredFile_beforeRemovingRow() {
        DocumentService service = newService();
        UUID id = UUID.randomUUID();
        Document doc = new Document();
        doc.setId(id);
        doc.setStorageKey(id + ".pdf");

        when(documentRepository.findById(id)).thenReturn(Optional.of(doc));

        service.deleteDocument(id);

        org.mockito.Mockito.verify(fileStorageService).delete(id + ".pdf");
        org.mockito.Mockito.verify(documentRepository).delete(doc);
    }
}
