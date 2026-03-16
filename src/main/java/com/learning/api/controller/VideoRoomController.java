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
/* import com.learning.api.service.ChatMessageService; */


/* @Controller
@RequiredArgsConstructor
public class VideoRoomController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/signal/{bookingId}")
    public void signal(@DestinationVariable Long bookingId, SignalingMessage message) {
        messagingTemplate.convertAndSend(
            "/topic/room/" + bookingId + "/signal", message);
    }

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

    @MessageMapping("/event/{bookingId}")
    public void event(@DestinationVariable Long bookingId, RoomEvent event) {
        messagingTemplate.convertAndSend(
            "/topic/room/" + bookingId + "/events", event);
    }
}
 */