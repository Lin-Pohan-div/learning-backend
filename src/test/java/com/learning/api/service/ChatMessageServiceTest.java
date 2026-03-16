package com.learning.api.service;

import com.learning.api.entity.ChatMessage;
import com.learning.api.entity.Order;
import com.learning.api.enums.MessageType;
import com.learning.api.repo.ChatMessageRepository;
import com.learning.api.repo.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private OrderRepository orderRepo;

    @InjectMocks
    private ChatMessageService chatMessageService;

    // ── 測試資料工廠 ──────────────────────────────────────────────────────────

    private ChatMessage makeMessage(Long id, Long orderId, int role, int type, String message) {
        ChatMessage msg = new ChatMessage();
        msg.setId(id);
        msg.setOrderId(orderId);
        msg.setRole(role);
        msg.setMessageType(type);
        msg.setMessage(message);
        return msg;
    }

    private Order makeOrder(Long id) {
        Order order = new Order();
        order.setId(id);
        return order;
    }

    // ── save ──────────────────────────────────────────────────────────────────

    @Test
    void save_textMessage_returnsSavedMessage() {
        when(orderRepo.findById(1L)).thenReturn(Optional.of(makeOrder(1L)));
        ChatMessage saved = makeMessage(10L, 1L, 1, MessageType.TEXT.getValue(), "Hello");
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(saved);

        ChatMessage result = chatMessageService.save(1L, 1, MessageType.TEXT.getValue(), "Hello", null);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getMessage()).isEqualTo("Hello");
        assertThat(result.getMessageType()).isEqualTo(MessageType.TEXT.getValue());
    }

    @Test
    void save_nullBookingId_throwsIllegalArgument() {
        assertThatThrownBy(() -> chatMessageService.save(null, 1, 1, "msg", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void save_zeroBookingId_throwsIllegalArgument() {
        assertThatThrownBy(() -> chatMessageService.save(0L, 1, 1, "msg", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void save_negativeBookingId_throwsIllegalArgument() {
        assertThatThrownBy(() -> chatMessageService.save(-1L, 1, 1, "msg", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void save_orderNotFound_throwsNoSuchElement() {
        when(orderRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatMessageService.save(999L, 1, 1, "msg", null))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void save_imageMessage_setsMediaUrlAndEmptyMessage() {
        when(orderRepo.findById(1L)).thenReturn(Optional.of(makeOrder(1L)));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(i -> i.getArgument(0));

        ChatMessage result = chatMessageService.save(1L, 1, MessageType.IMAGE.getValue(), null, "images/001.jpg");

        assertThat(result.getMediaUrl()).isEqualTo("images/001.jpg");
        assertThat(result.getMessage()).isEmpty();
        assertThat(result.getMessageType()).isEqualTo(MessageType.IMAGE.getValue());
    }

    @Test
    void save_voiceMessage_setsMediaUrl() {
        when(orderRepo.findById(1L)).thenReturn(Optional.of(makeOrder(1L)));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(i -> i.getArgument(0));

        ChatMessage result = chatMessageService.save(1L, 2, MessageType.VOICE.getValue(), null, "audio/001.mp3");

        assertThat(result.getMediaUrl()).isEqualTo("audio/001.mp3");
        assertThat(result.getMessageType()).isEqualTo(MessageType.VOICE.getValue());
    }

    @Test
    void save_nullMessageType_defaultsToText() {
        when(orderRepo.findById(1L)).thenReturn(Optional.of(makeOrder(1L)));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(i -> i.getArgument(0));

        ChatMessage result = chatMessageService.save(1L, 1, null, "msg", null);

        assertThat(result.getMessageType()).isEqualTo(MessageType.TEXT.getValue());
    }

    @Test
    void save_persistsWithCorrectRole() {
        when(orderRepo.findById(1L)).thenReturn(Optional.of(makeOrder(1L)));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(i -> i.getArgument(0));

        ChatMessage result = chatMessageService.save(1L, 2, MessageType.TEXT.getValue(), "tutor msg", null);

        assertThat(result.getRole()).isEqualTo(2);
    }

    // ── findByBookingId ───────────────────────────────────────────────────────

    @Test
    void findByBookingId_returnsSortedList() {
        ChatMessage m1 = makeMessage(1L, 100L, 1, 1, "hi");
        ChatMessage m2 = makeMessage(2L, 100L, 2, 1, "hello");
        when(chatMessageRepository.findByBookingIdOrderByCreatedAtAsc(100L)).thenReturn(List.of(m1, m2));

        List<ChatMessage> result = chatMessageService.findByBookingId(100L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMessage()).isEqualTo("hi");
    }

    @Test
    void findByBookingId_noMessages_returnsEmptyList() {
        when(chatMessageRepository.findByBookingIdOrderByCreatedAtAsc(99L)).thenReturn(List.of());

        List<ChatMessage> result = chatMessageService.findByBookingId(99L);

        assertThat(result).isEmpty();
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_existingMessage_returnsUpdatedMessage() {
        ChatMessage existing = makeMessage(1L, 100L, 1, 1, "old text");
        when(chatMessageRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(i -> i.getArgument(0));

        Optional<ChatMessage> result = chatMessageService.update(1L, "new text");

        assertThat(result).isPresent();
        assertThat(result.get().getMessage()).isEqualTo("new text");
    }

    @Test
    void update_nonExistingMessage_returnsEmpty() {
        when(chatMessageRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<ChatMessage> result = chatMessageService.update(999L, "new text");

        assertThat(result).isEmpty();
    }

    // ── deleteById ────────────────────────────────────────────────────────────

    @Test
    void deleteById_existingMessage_returnsTrue() {
        when(chatMessageRepository.existsById(1L)).thenReturn(true);

        boolean result = chatMessageService.deleteById(1L);

        assertThat(result).isTrue();
        verify(chatMessageRepository).deleteById(1L);
    }

    @Test
    void deleteById_nonExistingMessage_returnsFalse() {
        when(chatMessageRepository.existsById(999L)).thenReturn(false);

        boolean result = chatMessageService.deleteById(999L);

        assertThat(result).isFalse();
        verify(chatMessageRepository, never()).deleteById(any());
    }
}
