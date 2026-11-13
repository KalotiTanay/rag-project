package com.ragqa;

import com.ragqa.config.EmbeddingProperties;
import com.ragqa.embedding.EmbeddingClient;
import com.ragqa.embedding.EmbeddingService;
import com.ragqa.exception.EmbeddingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmbeddingServiceTest {

    @Mock
    private EmbeddingClient embeddingClient;

    @Test
    void splitsIntoMultipleBatches_andPreservesOrder() {
        EmbeddingProperties props = new EmbeddingProperties();
        props.setBatchSize(2);
        EmbeddingService service = new EmbeddingService(embeddingClient, props);

        List<String> texts = List.of("a", "b", "c", "d", "e");

        when(embeddingClient.embedBatch(List.of("a", "b")))
                .thenReturn(List.of(new float[]{1}, new float[]{2}));
        when(embeddingClient.embedBatch(List.of("c", "d")))
                .thenReturn(List.of(new float[]{3}, new float[]{4}));
        when(embeddingClient.embedBatch(List.of("e")))
                .thenReturn(List.of(new float[]{5}));

        List<float[]> result = service.embedAll(texts);

        assertThat(result).hasSize(5);
        assertThat(result.get(0)[0]).isEqualTo(1f);
        assertThat(result.get(4)[0]).isEqualTo(5f);
        verify(embeddingClient, times(3)).embedBatch(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void batchFailure_throwsEmbeddingExceptionIdentifyingBatch() {
        EmbeddingProperties props = new EmbeddingProperties();
        props.setBatchSize(2);
        EmbeddingService service = new EmbeddingService(embeddingClient, props);

        when(embeddingClient.embedBatch(List.of("a", "b")))
                .thenThrow(new RuntimeException("provider 500"));

        assertThatThrownBy(() -> service.embedAll(List.of("a", "b", "c")))
                .isInstanceOf(EmbeddingException.class)
                .hasMessageContaining("[0, 2)");
    }
}
