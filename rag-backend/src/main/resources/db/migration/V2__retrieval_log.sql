CREATE TABLE retrieval_logs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id     UUID REFERENCES conversations(id) ON DELETE SET NULL,
    question            TEXT NOT NULL,
    retrieved_chunk_ids UUID[] NOT NULL DEFAULT '{}',
    similarity_scores   DOUBLE PRECISION[] NOT NULL DEFAULT '{}',
    threshold_used      DOUBLE PRECISION NOT NULL,
    passed_threshold    BOOLEAN NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_retrieval_logs_conversation_id ON retrieval_logs (conversation_id);
CREATE INDEX idx_retrieval_logs_created_at ON retrieval_logs (created_at);
