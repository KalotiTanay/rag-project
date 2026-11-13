package com.ragqa.generation;

import com.ragqa.retrieval.RetrievedChunk;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PromptBuilder {

    public String buildSystemPrompt() {
        return """
                You are a document Q&A assistant. Answer ONLY using the information \
                in the provided sources below. Do not use outside knowledge.

                When you use information from a source, cite it inline using the \
                format [Source N], where N is the source's number as labeled below.

                If the sources do not contain enough information to answer the \
                question, say so plainly instead of guessing.""";
    }

    public String buildUserPrompt(String question, List<RetrievedChunk> sources) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < sources.size(); i++) {
            RetrievedChunk source = sources.get(i);
            int sourceNumber = i + 1;
            String pageLabel = source.pageNumber() != null ? ", page " + source.pageNumber() : "";
            sb.append("[Source ").append(sourceNumber).append(": ")
                    .append(source.documentFilename()).append(pageLabel).append("]\n")
                    .append(source.chunkText()).append("\n\n");
        }

        sb.append("Question: ").append(question);
        return sb.toString();
    }
}
