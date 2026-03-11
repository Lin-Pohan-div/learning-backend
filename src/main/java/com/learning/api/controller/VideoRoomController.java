package com.learning.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.learning.api.dto.ChatMessageRequest;
import com.learning.api.dto.RoomEvent;
import com.learning.api.dto.SignalingMessage;
import com.learning.api.entity.ChatMessage;
import com.learning.api.enums.MessageType;
import com.learning.api.service.ChatMessageService;

/**
 * 視訊聊天室 WebSocket 控制器
 *
 * 客戶端連線端點：  /ws (SockJS)
 * 客戶端訂閱主題：
 *   /topic/room/{bookingId}/signal  — WebRTC 信令
 *   /topic/room/{bookingId}/chat    — 即時聊天訊息
 *   /topic/room/{bookingId}/events  — 加入 / 離開事件
 *
 * 客戶端發送路徑：
 *   /app/signal/{bookingId}  — 傳送 WebRTC 信令
 *   /app/chat/{bookingId}    — 傳送聊天訊息（會持久化）
 *   /app/event/{bookingId}   — 傳送加入 / 離開事件
 */
@Controller
@RequiredArgsConstructor
public class VideoRoomController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    // ─── WebRTC 信令 ─────────────────────────────────────────────────────────

    /**
     * 中繼 WebRTC 信令（offer / answer / ICE candidate）給房間內另一端
     */
    @MessageMapping("/signal/{bookingId}")
    public void signal(@DestinationVariable Long bookingId, SignalingMessage message) {
        messagingTemplate.convertAndSend(
            "/topic/room/" + bookingId + "/signal", message);
    }

    // ─── 即時聊天 ─────────────────────────────────────────────────────────────

    /**
     * 接收聊天訊息，持久化後廣播給房間內所有人
     */
    @MessageMapping("/chat/{bookingId}")
    public void chat(@DestinationVariable Long bookingId, ChatMessageRequest request) {
        int typeValue = request.getMessageType() != null
            ? request.getMessageType()
            : MessageType.TEXT.getValue();

        ChatMessage saved = chatMessageService.save(
            bookingId,
            request.getRole(),
            typeValue,
            request.getMessage(),
            request.getMediaUrl()
        );

        messagingTemplate.convertAndSend(
            "/topic/room/" + bookingId + "/chat", saved);
    }

    // ─── 房間事件 ─────────────────────────────────────────────────────────────

    /**
     * 廣播加入 / 離開事件給房間內所有訂閱者
     */
    @MessageMapping("/event/{bookingId}")
    public void event(@DestinationVariable Long bookingId, RoomEvent event) {
        messagingTemplate.convertAndSend(
            "/topic/room/" + bookingId + "/events", event);
    }
}
