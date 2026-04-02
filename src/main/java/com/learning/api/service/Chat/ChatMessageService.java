package com.learning.api.service.Chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.learning.api.dto.ChatRoom.StudentConversationDTO;
import com.learning.api.entity.Bookings;
import com.learning.api.entity.ChatMessage;
import com.learning.api.entity.Tutor;
import com.learning.api.enums.MessageType;
import com.learning.api.repo.BookingRepository;
import com.learning.api.repo.ChatMessageRepository;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.OrderRepository;
import com.learning.api.repo.TutorRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final OrderRepository orderRepo;
    private final BookingRepository bookingRepo;
    private final TutorRepository tutorRepository;
    private final CourseRepo courseRepo;

    public List<ChatMessage> findByBookingId(Long bookingId) {
        return chatMessageRepository.findByBookingIdOrderByCreatedAtAsc(bookingId);
    }

    public ChatMessage save(Long bookingId, String role, Integer messageTypeValue, String message, String mediaUrl) {
        if (bookingId == null || bookingId <= 0) {
            throw new IllegalArgumentException("Booking ID 不能為空");
        }

        bookingRepo.findById(bookingId)
            .orElseThrow(() -> new NoSuchElementException("Booking ID: " + bookingId + " 不存在"));

        MessageType type = MessageType.fromValue(messageTypeValue != null ? messageTypeValue : MessageType.TEXT.getValue());

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setOrderId(bookingId);
        chatMessage.setRole(role);
        chatMessage.setMessageType(type.getValue());

        if (type.isMedia()) {
            chatMessage.setMediaUrl(mediaUrl);
            // 保存原始檔名到 message 欄位（用於下載時顯示正確檔名）
            if (message != null && !message.isBlank()) {
                chatMessage.setMessage(message);
            }
        } else {
            chatMessage.setMessage(message);
        }

        return chatMessageRepository.save(chatMessage);
    }

    public Optional<ChatMessage> update(Long id, String message) {
        return chatMessageRepository.findById(id).map(existing -> {
           /*  if (message == null || message.trim().isEmpty()) {
                throw new IllegalArgumentException("消息內容不能為空");
            } */
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

    public List<StudentConversationDTO> getStudentConversations(Long studentId) {
        List<Bookings> bookings = bookingRepo.findByStudentId(studentId);

        Map<Long, List<Bookings>> byTutor = bookings.stream()
            .collect(Collectors.groupingBy(Bookings::getTutorId));

        List<StudentConversationDTO> result = new ArrayList<>();
        for (Map.Entry<Long, List<Bookings>> entry : byTutor.entrySet()) {
            Long tutorId = entry.getKey();
            List<Long> bookingIds = entry.getValue().stream()
                .map(Bookings::getId).collect(Collectors.toList());

            List<ChatMessage> msgs = chatMessageRepository.findByOrderIdInOrderByCreatedAtAsc(bookingIds);
            ChatMessage last = msgs.isEmpty() ? null : msgs.get(msgs.size() - 1);

            Tutor tutor = tutorRepository.findById(tutorId).orElse(null);
            String participantName = tutor != null ? tutor.getUser().getName() : "未知老師";
            String avatar = tutor != null ? tutor.getAvatar() : null;

            String[] subjectHolder = {""};
            Long firstOrderId = entry.getValue().get(0).getOrderId();
            if (firstOrderId != null) {
                orderRepo.findById(firstOrderId).ifPresent(o ->
                    courseRepo.findById(o.getCourseId()).ifPresent(c -> subjectHolder[0] = c.getName())
                );
            }

            StudentConversationDTO dto = new StudentConversationDTO();
            dto.setOrderId(bookingIds.get(0));
            dto.setBookingRecordId(bookingIds.get(0));
            dto.setBookingIds(bookingIds);
            dto.setBookingRecordIds(bookingIds);
            dto.setParticipantId(tutorId);
            dto.setParticipantName(participantName);
            dto.setAvatar(avatar);
            dto.setSubject(subjectHolder[0]);
            dto.setLastMessage(last != null && last.getMessage() != null ? last.getMessage() : "");
            dto.setLastMessageTime(last != null ? last.getCreatedAt() : null);
            dto.setUnread(0);
            result.add(dto);
        }
        return result;
    }

    public List<ChatMessage> findByBookingIds(List<Long> ids) {
        return chatMessageRepository.findByOrderIdInOrderByCreatedAtAsc(ids);
    }
}
