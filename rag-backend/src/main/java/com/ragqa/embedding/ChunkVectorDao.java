package com.ragqa.embedding;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ChunkVectorDao {

    private final JdbcTemplate jdbcTemplate;

    public void updateEmbedding(UUID chunkId, float[] vector) {
        jdbcTemplate.update(
                "UPDATE chunks SET embedding = ?::vector WHERE id = ?",
                VectorFormat.toLiteral(vector), chunkId);
    }

    public void batchUpdateEmbeddings(Map<UUID, float[]> embeddingsByChunkId) {
        List<UUID> ids = List.copyOf(embeddingsByChunkId.keySet());
        jdbcTemplate.batchUpdate(
                "UPDATE chunks SET embedding = ?::vector WHERE id = ?",
                ids,
                ids.size(),
                (ps, id) -> {
                    ps.setString(1, VectorFormat.toLiteral(embeddingsByChunkId.get(id)));
                    ps.setObject(2, id);
                });
    }
}
