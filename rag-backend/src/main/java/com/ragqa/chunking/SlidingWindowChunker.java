package com.ragqa.chunking;

import com.ragqa.config.ChunkingProperties;
import com.ragqa.extraction.PageText;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SlidingWindowChunker {

    private final ChunkingProperties chunkingProperties;

    private record Word(String text, Integer pageNumber) {
    }

    public List<ChunkCandidate> chunk(List<PageText> pages) {
        return chunk(pages, chunkingProperties.getChunkSizeTokens(), chunkingProperties.getChunkOverlapTokens());
    }

    public List<ChunkCandidate> chunk(List<PageText> pages, int chunkSize, int overlap) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be positive");
        }
        if (overlap < 0 || overlap >= chunkSize) {
            throw new IllegalArgumentException("overlap must be >= 0 and less than chunkSize");
        }

        List<Word> words = flattenToWords(pages);
        List<ChunkCandidate> chunks = new ArrayList<>();

        if (words.isEmpty()) {
            return chunks;
        }

        int step = chunkSize - overlap;
        int chunkIndex = 0;

        for (int start = 0; start < words.size(); start += step) {
            int end = Math.min(start + chunkSize, words.size());
            List<Word> windowWords = words.subList(start, end);

            String text = windowWords.stream().map(Word::text).reduce((a, b) -> a + " " + b).orElse("");
            Integer pageNumber = windowWords.get(0).pageNumber();

            chunks.add(new ChunkCandidate(text, chunkIndex++, pageNumber));

            if (end == words.size()) {
                break;
            }
        }

        return chunks;
    }

    private List<Word> flattenToWords(List<PageText> pages) {
        List<Word> words = new ArrayList<>();
        for (PageText page : pages) {
            if (page.text() == null || page.text().isBlank()) {
                continue;
            }
            for (String w : page.text().split("\\s+")) {
                if (!w.isBlank()) {
                    words.add(new Word(w, page.pageNumber()));
                }
            }
        }
        return words;
    }
}
