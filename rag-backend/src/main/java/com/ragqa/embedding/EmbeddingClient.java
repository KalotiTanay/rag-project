package com.ragqa.embedding;

import java.util.List;

public interface EmbeddingClient {

    List<float[]> embedBatch(List<String> texts);
}
