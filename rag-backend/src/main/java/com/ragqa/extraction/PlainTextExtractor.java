package com.ragqa.extraction;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class PlainTextExtractor implements TextExtractor {

    @Override
    public boolean supports(String fileExtension) {
        return "txt".equalsIgnoreCase(fileExtension) || "md".equalsIgnoreCase(fileExtension);
    }

    @Override
    public List<PageText> extract(InputStream inputStream) throws IOException {
        String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        return List.of(new PageText(null, content.trim()));
    }
}
