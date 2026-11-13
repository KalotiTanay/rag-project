package com.ragqa.extraction;

import com.ragqa.exception.TextExtractionException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class TextExtractionService {

    private final List<TextExtractor> extractors;

    public TextExtractionService(List<TextExtractor> extractors) {
        this.extractors = extractors;
    }

    public List<PageText> extract(String fileExtension, InputStream inputStream) throws IOException {
        TextExtractor extractor = extractors.stream()
                .filter(e -> e.supports(fileExtension))
                .findFirst()
                .orElseThrow(() -> new TextExtractionException(
                        "No text extractor available for file type: " + fileExtension));
        return extractor.extract(inputStream);
    }
}
