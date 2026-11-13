package com.ragqa;

import com.ragqa.exception.ConversationNotFoundException;
import com.ragqa.model.Conversation;
import com.ragqa.model.MessageRole;
import com.ragqa.repository.ConversationRepository;
import com.ragqa.repository.MessageRepository;
import com.ragqa.service.ConversationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock private ConversationRepository conversationRepository;
    @Mock private MessageRepository messageRepository;

    private ConversationService newService() {
        return new ConversationService(conversationRepository, messageRepository);
    }

    @Test
    void resolveOrCreate_withNullId_createsNewConversation() {
        ConversationService service = newService();
        when(conversationRepository.save(org.mockito.ArgumentMatchers.any(Conversation.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Conversation result = service.resolveOrCreate(null);

        assertThat(result).isNotNull();
        verify(conversationRepository).save(org.mockito.ArgumentMatchers.any(Conversation.class));
    }

    @Test
    void resolveOrCreate_withExistingId_returnsIt() {
        ConversationService service = newService();
        UUID id = UUID.randomUUID();
        Conversation existing = new Conversation();
        existing.setId(id);
        when(conversationRepository.findById(id)).thenReturn(Optional.of(existing));

        Conversation result = service.resolveOrCreate(id);

        assertThat(result.getId()).isEqualTo(id);
        verify(conversationRepository, never()).save(any());
    }

    @Test
    void resolveOrCreate_withUnknownId_throws() {
        ConversationService service = newService();
        UUID id = UUID.randomUUID();
        when(conversationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolveOrCreate(id))
                .isInstanceOf(ConversationNotFoundException.class);
    }

    @Test
    void getMessages_throwsWhenConversationMissing() {
        ConversationService service = newService();
        UUID id = UUID.randomUUID();
        when(conversationRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.getMessages(id))
                .isInstanceOf(ConversationNotFoundException.class);
    }

    @Test
    void appendMessage_defaultsNullCitedChunkIdsToEmptyArray() {
        ConversationService service = newService();
        UUID conversationId = UUID.randomUUID();
        when(messageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var message = service.appendMessage(conversationId, MessageRole.user, "hello", null);

        assertThat(message.getCitedChunkIds()).isEmpty();
    }
}
