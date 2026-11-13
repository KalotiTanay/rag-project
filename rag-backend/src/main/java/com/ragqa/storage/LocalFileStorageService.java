package com.ragqa.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path rootDir;

    public LocalFileStorageService(@Value("${rag.storage.local-path}") String localPath) {
        this.rootDir = Path.of(localPath);
        try {
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create upload storage directory: " + rootDir, e);
        }
    }

    @Override
    public String store(MultipartFile file, UUID documentId) {

        String extension = extractExtension(file.getOriginalFilename());
        String storageKey = documentId + (extension.isEmpty() ? "" : "." + extension);
        Path target = rootDir.resolve(storageKey);

        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file for document " + documentId, e);
        }
        return storageKey;
    }

    @Override
    public InputStream load(String storageKey) {
        Path target = rootDir.resolve(storageKey);
        try {
            return Files.newInputStream(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load stored file: " + storageKey, e);
        }
    }

    @Override
    public void delete(String storageKey) {
        Path target = rootDir.resolve(storageKey);
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete stored file: " + storageKey, e);
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
    }
}
