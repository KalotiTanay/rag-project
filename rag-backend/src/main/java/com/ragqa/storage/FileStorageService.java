package com.ragqa.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

public interface FileStorageService {

    String store(MultipartFile file, UUID documentId);

    InputStream load(String storageKey);

    void delete(String storageKey);
}
