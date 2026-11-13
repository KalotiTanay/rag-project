package com.ragqa;

import com.ragqa.generation.PromptBuilder;
import com.ragqa.retrieval.RetrievedChunk;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PromptBuilderTest {

    private final PromptBuilder promptBuilder = new PromptBuilder();

    @Test
    void systemPrompt_instructsGroundingAndCitation() {
        String prompt = promptBuilder.buildSystemPrompt();
        assertThat(prompt).contains("ONLY").contains("[Source N]");
    }

    @Test
    void userPrompt_labelsEachSourceWithFilenameAndPage() {
        RetrievedChunk chunk1 = new RetrievedChunk(
                UUID.randomUUID(), UUID.randomUUID(), "policy.pdf", "Refunds within 30 days.", 4, 0.9);
        RetrievedChunk chunk2 = new RetrievedChunk(
                UUID.randomUUID(), UUID.randomUUID(), "notes.md", "General terms apply.", null, 0.8);

        String prompt = promptBuilder.buildUserPrompt("What is the refund policy?", List.of(chunk1, chunk2));

        assertThat(prompt).contains("[Source 1: policy.pdf, page 4]");
        assertThat(prompt).contains("[Source 2: notes.md]");
        assertThat(prompt).contains("Refunds within 30 days.");
        assertThat(prompt).contains("Question: What is the refund policy?");
    }
}
