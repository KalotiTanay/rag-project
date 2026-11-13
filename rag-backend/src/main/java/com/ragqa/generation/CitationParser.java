package com.ragqa.generation;

import com.ragqa.retrieval.RetrievedChunk;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CitationParser {

    private static final Pattern SOURCE_PATTERN = Pattern.compile("\\[Source (\\d+)]");

    public List<RetrievedChunk> extractCitedChunks(String answerText, List<RetrievedChunk> offeredSources) {
        Set<Integer> citedNumbers = new LinkedHashSet<>();
        Matcher matcher = SOURCE_PATTERN.matcher(answerText);
        while (matcher.find()) {
            citedNumbers.add(Integer.parseInt(matcher.group(1)));
        }

        return citedNumbers.stream()
                .filter(n -> n >= 1 && n <= offeredSources.size())
                .map(n -> offeredSources.get(n - 1))
                .toList();
    }
}
