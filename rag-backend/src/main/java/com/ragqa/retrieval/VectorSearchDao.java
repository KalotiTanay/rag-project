package com.ragqa.retrieval;

import com.ragqa.embedding.VectorFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class VectorSearchDao {

    private final JdbcTemplate jdbcTemplate;

    public List<RetrievedChunk> searchTopK(float[] queryVector, int topK) {
        String vectorLiteral = VectorFormat.toLiteral(queryVector);

        String sql = """
                SELECT c.id AS chunk_id,
                       c.document_id AS document_id,
                       d.filename AS document_filename,
                       c.chunk_text AS chunk_text,
                       c.page_number AS page_number,
                       1 - (c.embedding <=> ?::vector) AS similarity
                FROM chunks c
                JOIN documents d ON d.id = c.document_id
                WHERE d.status = 'READY'
                  AND c.embedding IS NOT NULL
                ORDER BY c.embedding <=> ?::vector
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new RetrievedChunk(
                (UUID) rs.getObject("chunk_id"),
                (UUID) rs.getObject("document_id"),
                rs.getString("document_filename"),
                rs.getString("chunk_text"),
                (Integer) rs.getObject("page_number"),
                rs.getDouble("similarity")
        ), vectorLiteral, vectorLiteral, topK);
    }
}
