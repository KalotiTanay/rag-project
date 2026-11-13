package com.ragqa;

import com.ragqa.config.RetrievalProperties;
import com.ragqa.embedding.EmbeddingService;
import com.ragqa.repository.RetrievalLogRepository;
import com.ragqa.retrieval.RetrievalService;
import com.ragqa.retrieval.RetrievedChunk;
import com.ragqa.retrieval.VectorSearchDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetrievalServiceTest {

    @Mock
    private EmbeddingService embeddingService;
    @Mock
    private VectorSearchDao vectorSearchDao;
    @Mock
    private RetrievalLogRepository retrievalLogRepository;

    private RetrievalService newService(RetrievalProperties props) {

        lenient().when(retrievalLogRepository.save(any())).thenReturn(null);
        return new RetrievalService(embeddingService, vectorSearchDao, props, retrievalLogRepository);
    }

    @Test
    void filtersOutChunksBelowThreshold() {
        RetrievalProperties props = new RetrievalProperties();
        props.setTopK(5);
        props.setSimilarityThreshold(0.75);
        RetrievalService service = newService(props);

        when(embeddingService.embedAll(List.of("what is the refund policy?")))
                .thenReturn(List.of(new float[]{0.1f, 0.2f}));

        RetrievedChunk aboveThreshold = new RetrievedChunk(
                UUID.randomUUID(), UUID.randomUUID(), "policy.pdf", "Refunds within 30 days.", 2, 0.90);
        RetrievedChunk belowThreshold = new RetrievedChunk(
                UUID.randomUUID(), UUID.randomUUID(), "unrelated.pdf", "Some other content.", 1, 0.40);

        when(vectorSearchDao.searchTopK(any(float[].class), anyInt()))
                .thenReturn(List.of(aboveThreshold, belowThreshold));

        List<RetrievedChunk> result = service.retrieve("what is the refund policy?");

        assertThat(result).containsExactly(aboveThreshold);
    }

    @Test
    void allChunksBelowThreshold_returnsEmptyList() {
        RetrievalProperties props = new RetrievalProperties();
        props.setSimilarityThreshold(0.75);
        RetrievalService service = newService(props);

        when(embeddingService.embedAll(List.of("random question")))
                .thenReturn(List.of(new float[]{0.1f}));
        when(vectorSearchDao.searchTopK(any(float[].class), anyInt()))
                .thenReturn(List.of(new RetrievedChunk(
                        UUID.randomUUID(), UUID.randomUUID(), "doc.pdf", "irrelevant", 1, 0.2)));

        List<RetrievedChunk> result = service.retrieve("random question");

        assertThat(result).isEmpty();
    }
}
