package com.learning.api.controller.ChatAndVideoController;

import com.learning.api.dto.ChatRoom.ChatMessageRequest;
import com.learning.api.dto.videoroom.RoomError;
import com.learning.api.dto.videoroom.RoomEvent;
import com.learning.api.dto.videoroom.SignalingMessage;
import com.learning.api.entity.Booking;
import com.learning.api.entity.ChatMessage;
import com.learning.api.enums.MessageType;
import com.learning.api.repo.BookingRepo;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;
import java.util.Optional;

import com.learning.api.security.SecurityUser;
import com.learning.api.service.Chat.ChatMessageService;
import com.learning.api.service.Chat.RoomService;

@Controller
@RequiredArgsConstructor
public class VideoRoomController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final BookingRepo bookingRepo;
    private final RoomService roomService;

    // ─── 信令轉發（WebRTC offer / answer / candidate）────────

    @MessageMapping("/signal/{bookingId}")
    public void signal(@DestinationVariable Long bookingId,
                       SignalingMessage message,
                       SimpMessageHeaderAccessor accessor) {
        try {
            if (!validateBookingAndRole(bookingId, parseRole(message.getSenderRole()), accessor)) return;
            messagingTemplate.convertAndSend("/topic/room/" + bookingId + "/signal", message);
        } catch (Exception e) {
            sendError(bookingId, accessor.getSessionId(), "SIGNAL_ERROR", e.getMessage());
        }
    }

    // ─── 聊天訊息（持久化 + 廣播）────────────────────────────

    @MessageMapping("/chat/{bookingId}")
    public void chat(@DestinationVariable Long bookingId,
                     ChatMessageRequest request,
                     SimpMessageHeaderAccessor accessor) {
        try {
            Integer roleNum = parseRole(request.getRole());
            if (!validateBookingAndRole(bookingId, roleNum, accessor)) return;

            int typeValue = request.getMessageType() != null
                    ? request.getMessageType()
                    : MessageType.TEXT.getValue();

            ChatMessage saved = chatMessageService.save(
                    bookingId,
                    normalizeRole(request.getRole()),
                    typeValue,
                    request.getMessage(),
                    request.getMediaUrl()
            );

            messagingTemplate.convertAndSend("/topic/room/" + bookingId + "/chat", saved);
        } catch (Exception e) {
            sendError(bookingId, accessor.getSessionId(), "CHAT_ERROR", e.getMessage());
        }
    }

    // ─── 房間事件（joined / left）+ RoomService 整合 ─────────

    @MessageMapping("/event/{bookingId}")
    public void event(@DestinationVariable Long bookingId,
                      RoomEvent event,
                      SimpMessageHeaderAccessor accessor) {
        try {
            if (!validateBookingAndRole(bookingId, event.getRole(), accessor)) return;

            String sessionId = accessor.getSessionId();
            Long userId = resolveUserId(accessor);

            // 將 userId 填入 event（供前端顯示）
            event.setUserId(userId);

            if ("joined".equals(event.getType())) {
                roomService.joinRoom(bookingId, userId, event.getRole(), sessionId);
            } else if ("left".equals(event.getType())) {
                roomService.leaveRoom(bookingId, sessionId);
            }

            messagingTemplate.convertAndSend("/topic/room/" + bookingId + "/events", event);
        } catch (Exception e) {
            sendError(bookingId, accessor.getSessionId(), "EVENT_ERROR", e.getMessage());
        }
    }

    // ─── 驗證輔助 ─────────────────────────────────────────────

    /**
     * 驗證 bookingId 存在、未取消，以及 role 合法（1 或 2）
     * @return true 表示驗證通過，false 表示已送出錯誤訊息
     */
    private boolean validateBookingAndRole(Long bookingId, Integer role, SimpMessageHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        Long userId = resolveUserId(accessor);

        // 0. 必須有 JWT userId
        if (userId == null) {
            sendError(bookingId, sessionId, "UNAUTHORIZED", "未登入或 JWT 無效，無法進入房間");
            return false;
        }

        // 1. bookingId 存在
        Optional<Booking> bookingOpt = bookingRepo.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            sendError(bookingId, sessionId, "BOOKING_NOT_FOUND",
                    "Booking " + bookingId + " 不存在");
            return false;
        }

        // 2. booking 未取消（status != 3）
        if (bookingOpt.get().getStatus() == 3) {
            sendError(bookingId, sessionId, "BOOKING_CANCELLED",
                    "Booking " + bookingId + " 已取消");
            return false;
        }

        // 3. role 合法
        if (role == null || (role != 1 && role != 2)) {
            sendError(bookingId, sessionId, "INVALID_ROLE",
                    "role 必須為 1（學生）或 2（導師），收到: " + role);
            return false;
        }

        // 4. JWT userId 必須符合 booking 中對應角色的使用者
        Booking booking = bookingOpt.get();
        boolean isOwner = (role == 1)
                ? userId.equals(booking.getStudentId())
                : userId.equals(booking.getTutorId());
        if (!isOwner) {
            sendError(bookingId, sessionId, "UNAUTHORIZED", "您無權進入此房間");
            return false;
        }

        return true;
    }

    private Integer parseRole(String role) {
        if ("student".equals(role) || "1".equals(role)) return 1;
        if ("tutor".equals(role)   || "2".equals(role)) return 2;
        return null;
    }

    private String normalizeRole(String role) {
        Integer num = parseRole(role);
        if (num == null) return role;
        return num == 1 ? "student" : "tutor";
    }

    /**
     * 廣播錯誤訊息至 /topic/room/{bookingId}/errors
     */
    private void sendError(Long bookingId, String sessionId, String code, String message) {
        RoomError error = new RoomError(code, message, Instant.now());
        messagingTemplate.convertAndSend("/topic/room/" + bookingId + "/errors", (Object) error);
    }

    /**
     * 從 STOMP Principal 取得 userId（由 WebSocketAuthInterceptor 設定）
     * 若尚未設定（測試模式無 JWT），回傳 null
     */
    private Long resolveUserId(SimpMessageHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal instanceof UsernamePasswordAuthenticationToken token
                && token.getPrincipal() instanceof SecurityUser securityUser) {
            return securityUser.getUser().getId();
        }
        return null;
    }
}
