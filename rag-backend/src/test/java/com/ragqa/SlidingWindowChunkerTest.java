package com.ragqa;

import com.ragqa.chunking.ChunkCandidate;
import com.ragqa.chunking.SlidingWindowChunker;
import com.ragqa.config.ChunkingProperties;
import com.ragqa.extraction.PageText;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SlidingWindowChunkerTest {

    private final SlidingWindowChunker chunker = new SlidingWindowChunker(new ChunkingProperties());

    @Test
    void textShorterThanOneChunk_producesSingleChunk() {
        PageText page = new PageText(1, "one two three four five");
        List<ChunkCandidate> chunks = chunker.chunk(List.of(page), 10, 2);

        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).text()).isEqualTo("one two three four five");
        assertThat(chunks.get(0).chunkIndex()).isEqualTo(0);
        assertThat(chunks.get(0).pageNumber()).isEqualTo(1);
    }

    @Test
    void exactMultiple_producesExpectedChunkCount() {

        String text = "w1 w2 w3 w4 w5 w6 w7 w8 w9 w10";
        List<ChunkCandidate> chunks = chunker.chunk(List.of(new PageText(1, text)), 5, 0);

        assertThat(chunks).hasSize(2);
        assertThat(chunks.get(0).text()).isEqualTo("w1 w2 w3 w4 w5");
        assertThat(chunks.get(1).text()).isEqualTo("w6 w7 w8 w9 w10");
    }

    @Test
    void unevenRemainder_lastChunkIsSmaller() {

        String text = "w1 w2 w3 w4 w5 w6 w7 w8 w9 w10 w11 w12";
        List<ChunkCandidate> chunks = chunker.chunk(List.of(new PageText(1, text)), 5, 0);

        assertThat(chunks).hasSize(3);
        assertThat(chunks.get(2).text()).isEqualTo("w11 w12");
    }

    @Test
    void overlapProducesOverlappingWindows() {
        String text = "w1 w2 w3 w4 w5 w6 w7 w8";
        List<ChunkCandidate> chunks = chunker.chunk(List.of(new PageText(1, text)), 4, 2);

        assertThat(chunks).hasSize(3);
        assertThat(chunks.get(0).text()).isEqualTo("w1 w2 w3 w4");
        assertThat(chunks.get(1).text()).isEqualTo("w3 w4 w5 w6");
        assertThat(chunks.get(2).text()).isEqualTo("w5 w6 w7 w8");
    }

    @Test
    void emptyInput_producesNoChunks() {
        List<ChunkCandidate> chunks = chunker.chunk(List.of(new PageText(1, "")), 5, 1);
        assertThat(chunks).isEmpty();
    }

    @Test
    void chunkTracksFirstWordsPageNumber_acrossPageBoundary() {
        PageText page1 = new PageText(1, "a b c");
        PageText page2 = new PageText(2, "d e f g h");

        List<ChunkCandidate> chunks = chunker.chunk(List.of(page1, page2), 4, 0);

        assertThat(chunks).hasSize(2);
        assertThat(chunks.get(0).pageNumber()).isEqualTo(1);
        assertThat(chunks.get(1).pageNumber()).isEqualTo(2);
    }
}
