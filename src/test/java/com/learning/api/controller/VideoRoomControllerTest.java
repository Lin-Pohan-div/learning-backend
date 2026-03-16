package com.learning.api.controller;

import com.learning.api.dto.ChatMessageRequest;
import com.learning.api.dto.RoomEvent;
import com.learning.api.dto.SignalingMessage;
import com.learning.api.entity.ChatMessage;
import com.learning.api.enums.MessageType;
import com.learning.api.service.ChatMessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * VideoRoomController 純單元測試（Mockito）
 * 不啟動 Spring Context，直接呼叫 @MessageMapping 方法並驗證 SimpMessagingTemplate。
 */
@ExtendWith(MockitoExtension.class)
class VideoRoomControllerTest {

    private static final Long BOOKING_ID = 42L;

    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private ChatMessageService chatMessageService;

    @InjectMocks
    private VideoRoomController videoRoomController;

    

    private SignalingMessage makeSignal(String type, Integer senderRole) {
        SignalingMessage msg = new SignalingMessage();
        msg.setType(type);
        msg.setSenderRole(senderRole);
        return msg;
    }

    private ChatMessageRequest makeChatReq(Integer messageType, String message, String mediaUrl) {
        ChatMessageRequest req = new ChatMessageRequest();
        req.setRole(1);
        req.setMessageType(messageType);
        req.setMessage(message);
        req.setMediaUrl(mediaUrl);
        return req;
    }

    private ChatMessage makeSavedMsg(Long id, int type) {
        ChatMessage msg = new ChatMessage();
        msg.setId(id);
        msg.setOrderId(BOOKING_ID);
        msg.setRole(1);
        msg.setMessageType(type);
        return msg;
    }

    private RoomEvent makeRoomEvent(String type, Integer role) {
        RoomEvent event = new RoomEvent();
        event.setType(type);
        event.setRole(role);
        return event;
    }

    // ── signal() — WebRTC 信令中繼 ────────────────────────────────────────────

    @Test
    void signal_offer_shouldRelayToCorrectTopic() {
        SignalingMessage msg = makeSignal("offer", 1);

        videoRoomController.signal(BOOKING_ID, msg);

        verify(messagingTemplate).convertAndSend("/topic/room/42/signal", msg);
    }

    @Test
    void signal_answer_shouldRelayToCorrectTopic() {
        SignalingMessage msg = makeSignal("answer", 2);

        videoRoomController.signal(BOOKING_ID, msg);

        verify(messagingTemplate).convertAndSend("/topic/room/42/signal", msg);
    }

    @Test
    void signal_iceCandidate_shouldRelayToCorrectTopic() {
        SignalingMessage msg = makeSignal("candidate", 1);
        msg.setCandidate("candidate:...");
        msg.setSdpMid("0");
        msg.setSdpMLineIndex(0);

        videoRoomController.signal(BOOKING_ID, msg);

        verify(messagingTemplate).convertAndSend("/topic/room/42/signal", msg);
    }

    @Test
    void signal_shouldNotInteractWithChatService() {
        videoRoomController.signal(BOOKING_ID, makeSignal("offer", 1));

        verifyNoInteractions(chatMessageService);
    }

    @Test
    void signal_differentBookingId_shouldUseDifferentTopic() {
        SignalingMessage msg = makeSignal("offer", 1);

        videoRoomController.signal(99L, msg);

        verify(messagingTemplate).convertAndSend("/topic/room/99/signal", msg);
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/room/42/signal"), any(Object.class));
    }

    // ── chat() — 即時聊天訊息（文字）────────────────────────────────────────

    @Test
    void chat_textMessage_shouldSaveAndBroadcast() {
        ChatMessageRequest req = makeChatReq(MessageType.TEXT.getValue(), "Hello!", null);
        ChatMessage saved = makeSavedMsg(1L, MessageType.TEXT.getValue());
        when(chatMessageService.save(BOOKING_ID, 1, MessageType.TEXT.getValue(), "Hello!", null))
                .thenReturn(saved);

        videoRoomController.chat(BOOKING_ID, req);

        verify(chatMessageService).save(BOOKING_ID, 1, MessageType.TEXT.getValue(), "Hello!", null);
        verify(messagingTemplate).convertAndSend("/topic/room/42/chat", saved);
    }

    @Test
    void chat_nullMessageType_shouldDefaultToText() {
        ChatMessageRequest req = makeChatReq(null, "Hello!", null);
        ChatMessage saved = makeSavedMsg(1L, MessageType.TEXT.getValue());
        when(chatMessageService.save(BOOKING_ID, 1, MessageType.TEXT.getValue(), "Hello!", null))
                .thenReturn(saved);

        videoRoomController.chat(BOOKING_ID, req);

        verify(chatMessageService).save(BOOKING_ID, 1, MessageType.TEXT.getValue(), "Hello!", null);
    }

    @Test
    void chat_broadcastContainsPersistedEntity() {
        ChatMessageRequest req = makeChatReq(MessageType.TEXT.getValue(), "Hi", null);
        ChatMessage saved = makeSavedMsg(99L, MessageType.TEXT.getValue());
        when(chatMessageService.save(any(), any(), any(), any(), any())).thenReturn(saved);

        videoRoomController.chat(BOOKING_ID, req);

        verify(messagingTemplate).convertAndSend(eq("/topic/room/42/chat"),
                argThat((ChatMessage msg) -> msg.getId().equals(99L)));
    }

    @Test
    void chat_shouldNotBroadcastToSignalOrEventsTopic() {
        ChatMessageRequest req = makeChatReq(MessageType.TEXT.getValue(), "Hi", null);
        when(chatMessageService.save(any(), any(), any(), any(), any())).thenReturn(makeSavedMsg(1L, 1));

        videoRoomController.chat(BOOKING_ID, req);

        verify(messagingTemplate, never()).convertAndSend(eq("/topic/room/42/signal"), any(Object.class));
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/room/42/events"), any(Object.class));
    }

    // ── chat() — 即時聊天訊息（媒體）────────────────────────────────────────

    @Test
    void chat_stickerMessage_shouldSaveAndBroadcast() {
        ChatMessageRequest req = makeChatReq(MessageType.STICKER.getValue(), null, "stickers/001.png");
        req.setRole(2);
        ChatMessage saved = makeSavedMsg(2L, MessageType.STICKER.getValue());
        when(chatMessageService.save(BOOKING_ID, 2, MessageType.STICKER.getValue(), null, "stickers/001.png"))
                .thenReturn(saved);

        videoRoomController.chat(BOOKING_ID, req);

        verify(chatMessageService).save(BOOKING_ID, 2, MessageType.STICKER.getValue(), null, "stickers/001.png");
        verify(messagingTemplate).convertAndSend("/topic/room/42/chat", saved);
    }

    @Test
    void chat_voiceMessage_shouldSaveAndBroadcast() {
        ChatMessageRequest req = makeChatReq(MessageType.VOICE.getValue(), null, "audio/001.mp3");
        req.setRole(2);
        ChatMessage saved = makeSavedMsg(3L, MessageType.VOICE.getValue());
        when(chatMessageService.save(BOOKING_ID, 2, MessageType.VOICE.getValue(), null, "audio/001.mp3"))
                .thenReturn(saved);

        videoRoomController.chat(BOOKING_ID, req);

        verify(chatMessageService).save(BOOKING_ID, 2, MessageType.VOICE.getValue(), null, "audio/001.mp3");
    }

    @Test
    void chat_imageMessage_shouldSaveAndBroadcast() {
        ChatMessageRequest req = makeChatReq(MessageType.IMAGE.getValue(), null, "images/001.jpg");
        req.setRole(2);
        ChatMessage saved = makeSavedMsg(4L, MessageType.IMAGE.getValue());
        when(chatMessageService.save(BOOKING_ID, 2, MessageType.IMAGE.getValue(), null, "images/001.jpg"))
                .thenReturn(saved);

        videoRoomController.chat(BOOKING_ID, req);

        verify(chatMessageService).save(BOOKING_ID, 2, MessageType.IMAGE.getValue(), null, "images/001.jpg");
    }

    @Test
    void chat_videoMessage_shouldSaveAndBroadcast() {
        ChatMessageRequest req = makeChatReq(MessageType.VIDEO.getValue(), null, "videos/001.mp4");
        req.setRole(2);
        ChatMessage saved = makeSavedMsg(5L, MessageType.VIDEO.getValue());
        when(chatMessageService.save(BOOKING_ID, 2, MessageType.VIDEO.getValue(), null, "videos/001.mp4"))
                .thenReturn(saved);

        videoRoomController.chat(BOOKING_ID, req);

        verify(chatMessageService).save(BOOKING_ID, 2, MessageType.VIDEO.getValue(), null, "videos/001.mp4");
    }

    // ── event() — 房間加入/離開事件 ──────────────────────────────────────────

    @Test
    void event_joined_shouldBroadcastToCorrectTopic() {
        RoomEvent event = makeRoomEvent("joined", 1);

        videoRoomController.event(BOOKING_ID, event);

        verify(messagingTemplate).convertAndSend("/topic/room/42/events", event);
    }

    @Test
    void event_left_shouldBroadcastToCorrectTopic() {
        RoomEvent event = makeRoomEvent("left", 2);

        videoRoomController.event(BOOKING_ID, event);

        verify(messagingTemplate).convertAndSend("/topic/room/42/events", event);
    }

    @Test
    void event_shouldNotInteractWithChatService() {
        videoRoomController.event(BOOKING_ID, makeRoomEvent("joined", 1));

        verifyNoInteractions(chatMessageService);
    }

    @Test
    void event_differentBookingId_shouldUseDifferentTopic() {
        RoomEvent event = makeRoomEvent("joined", 1);

        videoRoomController.event(100L, event);

        verify(messagingTemplate).convertAndSend("/topic/room/100/events", event);
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/room/42/events"), any(Object.class));
    }
}
