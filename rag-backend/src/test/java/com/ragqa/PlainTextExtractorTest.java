package com.ragqa;

import com.ragqa.extraction.PageText;
import com.ragqa.extraction.PlainTextExtractor;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlainTextExtractorTest {

    private final PlainTextExtractor extractor = new PlainTextExtractor();

    @Test
    void supportsTxtAndMd() {
        assertThat(extractor.supports("txt")).isTrue();
        assertThat(extractor.supports("md")).isTrue();
        assertThat(extractor.supports("pdf")).isFalse();
    }

    @Test
    void extractsWholeContentAsSinglePagelessBlock() throws Exception {
        String content = "  Hello world, this is a test file.  ";
        List<PageText> pages = extractor.extract(
                new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));

        assertThat(pages).hasSize(1);
        assertThat(pages.get(0).pageNumber()).isNull();
        assertThat(pages.get(0).text()).isEqualTo("Hello world, this is a test file.");
    }
}
