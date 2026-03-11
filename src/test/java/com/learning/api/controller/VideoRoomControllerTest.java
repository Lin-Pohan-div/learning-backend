package com.learning.api.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.learning.api.dto.ChatMessageRequest;
import com.learning.api.dto.RoomEvent;
import com.learning.api.dto.SignalingMessage;
import com.learning.api.entity.ChatMessage;
import com.learning.api.enums.MessageType;
import com.learning.api.service.ChatMessageService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoRoomControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private VideoRoomController controller;

    private static final Long BOOKING_ID = 42L;

    // ===================== signal() =====================

    @Test
    void signal_offer_shouldRelayToCorrectTopic() {
        SignalingMessage msg = new SignalingMessage();
        msg.setType("offer");
        msg.setSenderRole(1);
        msg.setSdp("v=0\r\no=- 0 0 IN IP4 127.0.0.1\r\n");

        controller.signal(BOOKING_ID, msg);

        verify(messagingTemplate).convertAndSend("/topic/room/42/signal", msg);
    }

    @Test
    void signal_answer_shouldRelayToCorrectTopic() {
        SignalingMessage msg = new SignalingMessage();
        msg.setType("answer");
        msg.setSenderRole(2);
        msg.setSdp("v=0\r\no=- 0 0 IN IP4 127.0.0.1\r\n");

        controller.signal(BOOKING_ID, msg);

        verify(messagingTemplate).convertAndSend("/topic/room/42/signal", msg);
    }

    @Test
    void signal_iceCandidate_shouldRelayToCorrectTopic() {
        SignalingMessage msg = new SignalingMessage();
        msg.setType("candidate");
        msg.setSenderRole(1);
        msg.setCandidate("candidate:foundation 1 udp 2122260223 192.168.1.1 50000 typ host");
        msg.setSdpMid("0");
        msg.setSdpMLineIndex(0);

        controller.signal(BOOKING_ID, msg);

        verify(messagingTemplate).convertAndSend("/topic/room/42/signal", msg);
    }

    @Test
    void signal_shouldNotInteractWithChatService() {
        controller.signal(BOOKING_ID, new SignalingMessage());

        verifyNoInteractions(chatMessageService);
    }

    @Test
    void signal_differentBookingId_shouldUseDifferentTopic() {
        SignalingMessage msg = new SignalingMessage();
        msg.setType("offer");

        controller.signal(99L, msg);

        verify(messagingTemplate).convertAndSend("/topic/room/99/signal", msg);
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/room/42/signal"), (Object) any());
    }

    // ===================== chat() — 文字訊息 =====================

    @Test
    void chat_textMessage_shouldSaveAndBroadcast() {
        ChatMessageRequest request = buildTextRequest("Hello!");
        ChatMessage saved = buildSavedChatMessage(1L, "Hello!", MessageType.TEXT.getValue(), null);

        when(chatMessageService.save(BOOKING_ID, 1, MessageType.TEXT.getValue(), "Hello!", null))
            .thenReturn(saved);

        controller.chat(BOOKING_ID, request);

        verify(chatMessageService).save(BOOKING_ID, 1, MessageType.TEXT.getValue(), "Hello!", null);
        verify(messagingTemplate).convertAndSend("/topic/room/42/chat", saved);
    }

    @Test
    void chat_nullMessageType_shouldDefaultToText() {
        ChatMessageRequest request = new ChatMessageRequest();
        request.setBookingId(BOOKING_ID);
        request.setRole(1);
        request.setMessageType(null);
        request.setMessage("Hello!");

        when(chatMessageService.save(BOOKING_ID, 1, MessageType.TEXT.getValue(), "Hello!", null))
            .thenReturn(new ChatMessage());

        controller.chat(BOOKING_ID, request);

        verify(chatMessageService).save(BOOKING_ID, 1, MessageType.TEXT.getValue(), "Hello!", null);
    }

    @Test
    void chat_broadcastContainsPersistedEntity() {
        ChatMessageRequest request = buildTextRequest("Persist me");
        ChatMessage saved = buildSavedChatMessage(99L, "Persist me", MessageType.TEXT.getValue(), null);

        when(chatMessageService.save(any(), any(), any(), any(), any())).thenReturn(saved);

        controller.chat(BOOKING_ID, request);

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/room/42/chat"), captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(99L);
    }

    // ===================== chat() — 媒體訊息 =====================

    @Test
    void chat_stickerMessage_shouldSaveAndBroadcast() {
        String url = "https://example.com/stickers/001.png";
        ChatMessageRequest request = buildMediaRequest(MessageType.STICKER.getValue(), url);
        ChatMessage saved = buildSavedChatMessage(2L, null, MessageType.STICKER.getValue(), url);

        when(chatMessageService.save(BOOKING_ID, 2, MessageType.STICKER.getValue(), null, url))
            .thenReturn(saved);

        controller.chat(BOOKING_ID, request);

        verify(chatMessageService).save(BOOKING_ID, 2, MessageType.STICKER.getValue(), null, url);
        verify(messagingTemplate).convertAndSend("/topic/room/42/chat", saved);
    }

    @Test
    void chat_voiceMessage_shouldSaveAndBroadcast() {
        String url = "https://example.com/audio/001.mp3";
        ChatMessageRequest request = buildMediaRequest(MessageType.VOICE.getValue(), url);
        ChatMessage saved = buildSavedChatMessage(3L, null, MessageType.VOICE.getValue(), url);

        when(chatMessageService.save(BOOKING_ID, 2, MessageType.VOICE.getValue(), null, url))
            .thenReturn(saved);

        controller.chat(BOOKING_ID, request);

        verify(chatMessageService).save(BOOKING_ID, 2, MessageType.VOICE.getValue(), null, url);
        verify(messagingTemplate).convertAndSend("/topic/room/42/chat", saved);
    }

    @Test
    void chat_imageMessage_shouldSaveAndBroadcast() {
        String url = "https://example.com/images/001.jpg";
        ChatMessageRequest request = buildMediaRequest(MessageType.IMAGE.getValue(), url);
        ChatMessage saved = buildSavedChatMessage(4L, null, MessageType.IMAGE.getValue(), url);

        when(chatMessageService.save(BOOKING_ID, 2, MessageType.IMAGE.getValue(), null, url))
            .thenReturn(saved);

        controller.chat(BOOKING_ID, request);

        verify(chatMessageService).save(BOOKING_ID, 2, MessageType.IMAGE.getValue(), null, url);
        verify(messagingTemplate).convertAndSend("/topic/room/42/chat", saved);
    }

    @Test
    void chat_videoMessage_shouldSaveAndBroadcast() {
        String url = "https://example.com/videos/001.mp4";
        ChatMessageRequest request = buildMediaRequest(MessageType.VIDEO.getValue(), url);
        ChatMessage saved = buildSavedChatMessage(5L, null, MessageType.VIDEO.getValue(), url);

        when(chatMessageService.save(BOOKING_ID, 2, MessageType.VIDEO.getValue(), null, url))
            .thenReturn(saved);

        controller.chat(BOOKING_ID, request);

        verify(chatMessageService).save(BOOKING_ID, 2, MessageType.VIDEO.getValue(), null, url);
        verify(messagingTemplate).convertAndSend("/topic/room/42/chat", saved);
    }

    @Test
    void chat_shouldNotBroadcastToSignalOrEventsTopic() {
        when(chatMessageService.save(any(), any(), any(), any(), any())).thenReturn(new ChatMessage());

        controller.chat(BOOKING_ID, buildTextRequest("Hi"));

        verify(messagingTemplate, never()).convertAndSend(eq("/topic/room/42/signal"), (Object) any());
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/room/42/events"), (Object) any());
    }

    // ===================== event() =====================

    @Test
    void event_joined_shouldBroadcastToCorrectTopic() {
        RoomEvent event = buildRoomEvent("joined", 1);

        controller.event(BOOKING_ID, event);

        verify(messagingTemplate).convertAndSend("/topic/room/42/events", event);
    }

    @Test
    void event_left_shouldBroadcastToCorrectTopic() {
        RoomEvent event = buildRoomEvent("left", 2);

        controller.event(BOOKING_ID, event);

        verify(messagingTemplate).convertAndSend("/topic/room/42/events", event);
    }

    @Test
    void event_shouldNotInteractWithChatService() {
        controller.event(BOOKING_ID, buildRoomEvent("joined", 1));

        verifyNoInteractions(chatMessageService);
    }

    @Test
    void event_differentBookingId_shouldUseDifferentTopic() {
        RoomEvent event = buildRoomEvent("joined", 1);

        controller.event(100L, event);

        verify(messagingTemplate).convertAndSend("/topic/room/100/events", event);
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/room/42/events"), (Object) any());
    }

    // ===================== Helper Methods =====================

    private ChatMessageRequest buildTextRequest(String message) {
        ChatMessageRequest req = new ChatMessageRequest();
        req.setBookingId(BOOKING_ID);
        req.setRole(1);
        req.setMessageType(MessageType.TEXT.getValue());
        req.setMessage(message);
        return req;
    }

    private ChatMessageRequest buildMediaRequest(int messageType, String mediaUrl) {
        ChatMessageRequest req = new ChatMessageRequest();
        req.setBookingId(BOOKING_ID);
        req.setRole(2);
        req.setMessageType(messageType);
        req.setMediaUrl(mediaUrl);
        return req;
    }

    private ChatMessage buildSavedChatMessage(Long id, String message, int messageType, String mediaUrl) {
        ChatMessage msg = new ChatMessage();
        msg.setId(id);
        msg.setOrderId(BOOKING_ID);
        msg.setMessageType(messageType);
        msg.setMessage(message);
        msg.setMediaUrl(mediaUrl);
        return msg;
    }

    private RoomEvent buildRoomEvent(String type, int role) {
        RoomEvent event = new RoomEvent();
        event.setType(type);
        event.setRole(role);
        return event;
    }
}
