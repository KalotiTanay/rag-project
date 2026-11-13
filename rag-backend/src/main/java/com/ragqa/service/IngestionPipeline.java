package com.ragqa.service;

import java.util.UUID;

public interface IngestionPipeline {

    void process(UUID documentId) throws Exception;
}
