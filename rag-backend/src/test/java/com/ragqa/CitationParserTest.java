package com.ragqa;

import com.ragqa.generation.CitationParser;
import com.ragqa.retrieval.RetrievedChunk;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CitationParserTest {

    private final CitationParser parser = new CitationParser();

    private RetrievedChunk chunk(String filename) {
        return new RetrievedChunk(UUID.randomUUID(), UUID.randomUUID(), filename, "text", 1, 0.9);
    }

    @Test
    void extractsCitedSourcesInOrder() {
        RetrievedChunk source1 = chunk("a.pdf");
        RetrievedChunk source2 = chunk("b.pdf");
        String answer = "Refunds are allowed [Source 1]. See also policy [Source 2].";

        List<RetrievedChunk> cited = parser.extractCitedChunks(answer, List.of(source1, source2));

        assertThat(cited).containsExactly(source1, source2);
    }

    @Test
    void dropsOutOfRangeCitationNumbers() {
        RetrievedChunk source1 = chunk("a.pdf");
        String answer = "According to [Source 1] and also [Source 5] (fabricated).";

        List<RetrievedChunk> cited = parser.extractCitedChunks(answer, List.of(source1));

        assertThat(cited).containsExactly(source1);
    }

    @Test
    void noCitationMarkers_returnsEmptyList() {
        RetrievedChunk source1 = chunk("a.pdf");
        String answer = "This is a plain answer with no citations.";

        List<RetrievedChunk> cited = parser.extractCitedChunks(answer, List.of(source1));

        assertThat(cited).isEmpty();
    }

    @Test
    void duplicateCitations_deduplicated() {
        RetrievedChunk source1 = chunk("a.pdf");
        String answer = "[Source 1] ... and again [Source 1].";

        List<RetrievedChunk> cited = parser.extractCitedChunks(answer, List.of(source1));

        assertThat(cited).containsExactly(source1);
    }
}
