package com.ragqa.controller;

import com.ragqa.dto.QueryRequest;
import com.ragqa.dto.QueryResponse;
import com.ragqa.generation.AnswerGenerationService;
import com.ragqa.generation.StreamingAnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class QueryController {

    private final AnswerGenerationService answerGenerationService;
    private final StreamingAnswerService streamingAnswerService;

    @PostMapping("/query")
    public QueryResponse query(@Valid @RequestBody QueryRequest request) {
        return answerGenerationService.generateAnswer(request.conversationId(), request.question());
    }

    @PostMapping(path = "/query/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter queryStream(@Valid @RequestBody QueryRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L);
        streamingAnswerService.streamAnswer(request.conversationId(), request.question(), emitter);
        return emitter;
    }
}
