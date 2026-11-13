package com.ragqa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "retrieval_logs")
@Getter
@Setter
@NoArgsConstructor
public class RetrievalLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "conversation_id")
    private UUID conversationId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "retrieved_chunk_ids", columnDefinition = "uuid[]", nullable = false)
    private UUID[] retrievedChunkIds = new UUID[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "similarity_scores", columnDefinition = "double precision[]", nullable = false)
    private Double[] similarityScores = new Double[0];

    @Column(name = "threshold_used", nullable = false)
    private double thresholdUsed;

    @Column(name = "passed_threshold", nullable = false)
    private boolean passedThreshold;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
