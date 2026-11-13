package com.ragqa.controller;

import com.ragqa.dto.ConversationSummary;
import com.ragqa.dto.MessageResponse;
import com.ragqa.model.Conversation;
import com.ragqa.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping
    public List<ConversationSummary> listConversations() {
        return conversationService.listConversations().stream()
                .map(ConversationSummary::fromEntity)
                .toList();
    }

    @PostMapping
    public ResponseEntity<ConversationSummary> createConversation() {
        Conversation conversation = conversationService.createConversation();
        return ResponseEntity.status(HttpStatus.CREATED).body(ConversationSummary.fromEntity(conversation));
    }

    @GetMapping("/{id}/messages")
    public List<MessageResponse> getMessages(@PathVariable UUID id) {
        return conversationService.getMessages(id).stream()
                .map(MessageResponse::fromEntity)
                .toList();
    }
}
