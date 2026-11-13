package com.ragqa.service;

import com.ragqa.config.IngestionProperties;
import com.ragqa.exception.InvalidFileException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class FileValidator {

    private final IngestionProperties ingestionProperties;

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Uploaded file is empty or missing.");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw new InvalidFileException("File must have a recognizable extension (pdf, txt, md).");
        }

        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        if (!ingestionProperties.getAllowedFileTypes().contains(extension)) {
            throw new InvalidFileException(
                    "Unsupported file type '." + extension + "'. Allowed types: "
                            + ingestionProperties.getAllowedFileTypes());
        }

        long maxBytes = (long) ingestionProperties.getMaxFileSizeMb() * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new InvalidFileException(
                    "File exceeds max size of " + ingestionProperties.getMaxFileSizeMb() + "MB.");
        }
    }
}
