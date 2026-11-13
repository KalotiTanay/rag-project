package com.ragqa.controller;

import com.ragqa.retrieval.RetrievalService;
import com.ragqa.retrieval.RetrievedChunk;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RetrievalDebugController {

    private final RetrievalService retrievalService;

    @Value("${rag.debug.retrieval-debug-enabled:true}")
    private boolean retrievalDebugEnabled;

    @GetMapping("/retrieval/debug")
    public ResponseEntity<List<RetrievedChunk>> debugRetrieve(@RequestParam String question) {
        if (!retrievalDebugEnabled) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(retrievalService.retrieve(question));
    }
}
