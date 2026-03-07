package com.learning.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.learning.api.entity.ChatMessage;
import com.learning.api.repo.BookingRepository;
import com.learning.api.repo.ChatMessageRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final BookingRepository bookingRepository;

    public List<ChatMessage> findByBookingId(Long bookingId) {
        return chatMessageRepository.findByBookingIdOrderByCreatedAtAsc(bookingId);
    }

    public ChatMessage save(Long bookingId, Byte role, String message) {
        if (bookingId == null || bookingId <= 0) {
            throw new IllegalArgumentException("Booking ID 不能為空");
        }

        bookingRepository.findById(bookingId)
            .orElseThrow(() -> new NoSuchElementException("Booking ID: " + bookingId + " 不存在"));

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setBookingId(bookingId);
        chatMessage.setRole(role);
        chatMessage.setMessage(message);

        return chatMessageRepository.save(chatMessage);
    }

    public Optional<ChatMessage> update(Long id, String message) {
        return chatMessageRepository.findById(id).map(existing -> {
            if (message == null || message.trim().isEmpty()) {
                throw new IllegalArgumentException("消息內容不能為空");
            }
            existing.setMessage(message);
            return chatMessageRepository.save(existing);
        });
    }

    public boolean deleteById(Long id) {
        if (chatMessageRepository.existsById(id)) {
            chatMessageRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
