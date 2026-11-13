package com.ragqa.dto;

import com.ragqa.model.Document;
import com.ragqa.model.DocumentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String filename,
        DocumentStatus status,
        String failureReason,
        OffsetDateTime uploadDate
) {
    public static DocumentResponse fromEntity(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getFilename(),
                document.getStatus(),
                document.getFailureReason(),
                document.getUploadDate()
        );
    }
}
