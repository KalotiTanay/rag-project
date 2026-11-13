package com.ragqa.service;

import com.ragqa.exception.ConversationNotFoundException;
import com.ragqa.model.Conversation;
import com.ragqa.model.Message;
import com.ragqa.model.MessageRole;
import com.ragqa.repository.ConversationRepository;
import com.ragqa.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    private static final String DEFAULT_OWNER = "default-user";

    @Transactional
    public Conversation createConversation() {
        Conversation conversation = new Conversation();
        conversation.setOwnerId(DEFAULT_OWNER);
        return conversationRepository.save(conversation);
    }

    @Transactional
    public Conversation resolveOrCreate(UUID conversationId) {
        if (conversationId == null) {
            return createConversation();
        }
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(conversationId));
    }

    @Transactional(readOnly = true)
    public List<Conversation> listConversations() {
        return conversationRepository.findAllByOwnerIdOrderByCreatedAtDesc(DEFAULT_OWNER);
    }

    @Transactional(readOnly = true)
    public List<Message> getMessages(UUID conversationId) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new ConversationNotFoundException(conversationId);
        }
        return messageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Transactional
    public Message appendMessage(UUID conversationId, MessageRole role, String content, UUID[] citedChunkIds) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setCitedChunkIds(citedChunkIds != null ? citedChunkIds : new UUID[0]);
        return messageRepository.save(message);
    }
}
