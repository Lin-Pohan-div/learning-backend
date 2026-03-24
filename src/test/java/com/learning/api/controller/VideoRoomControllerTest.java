package com.learning.api.controller;

import com.learning.api.controller.ChatAndVideoController.VideoRoomController;
import com.learning.api.dto.ChatRoom.ChatMessageRequest;
import com.learning.api.dto.videoroom.RoomEvent;
import com.learning.api.dto.videoroom.SignalingMessage;
import com.learning.api.entity.Bookings;
import com.learning.api.entity.ChatMessage;
import com.learning.api.entity.User;
import com.learning.api.enums.MessageType;
import com.learning.api.repo.BookingRepo;
import com.learning.api.security.SecurityUser;
import com.learning.api.service.Chat.ChatMessageService;
import com.learning.api.service.Chat.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * VideoRoomController 純單元測試（Mockito）
 * 不啟動 Spring Context，直接呼叫 @MessageMapping 方法並驗證 SimpMessagingTemplate。
 */
@ExtendWith(MockitoExtension.class)
class VideoRoomControllerTest {

    private static final Long BOOKING_ID = 42L;
    private static final Long STUDENT_ID = 1L;
    private static final Long TUTOR_ID   = 2L;

    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private ChatMessageService    chatMessageService;
    @Mock private BookingRepo           bookingRepo;
    @Mock private RoomService           roomService;

    @InjectMocks
    private VideoRoomController videoRoomController;

    @BeforeEach
    void setUp() {
        Bookings booking = new Bookings();
        booking.setId(BOOKING_ID);
        booking.setStudentId(STUDENT_ID);
        booking.setTutorId(TUTOR_ID);
        booking.setStatus(1);
        booking.setDate(LocalDate.now());
        booking.setHour(10);
        lenient().when(bookingRepo.findById(anyLong())).thenReturn(Optional.of(booking));
    }

    /** 建立帶有正確 JWT principal 的 SimpMessageHeaderAccessor mock。*/
    private SimpMessageHeaderAccessor makeAccessor(int role) {
        SimpMessageHeaderAccessor acc = mock(SimpMessageHeaderAccessor.class);
        Long userId = (role == 1) ? STUDENT_ID : TUTOR_ID;
        User user = new User();
        user.setId(userId);
        SecurityUser securityUser = new SecurityUser(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(securityUser, null);
        when(acc.getUser()).thenReturn(auth);
        lenient().when(acc.getSessionId()).thenReturn("test-session");
        return acc;
    }

    private SignalingMessage makeSignal(String type, String senderRole) {
        SignalingMessage msg = new SignalingMessage();
        msg.setType(type);
        msg.setSenderRole(senderRole);
        return msg;
    }

    private ChatMessageRequest makeChatReq(Integer messageType, String message, String mediaUrl) {
        ChatMessageRequest req = new ChatMessageRequest();
        req.setRole("1");
        req.setMessageType(messageType);
        req.setMessage(message);
        req.setMediaUrl(mediaUrl);
        return req;
    }

    private ChatMessage makeSavedMsg(Long id, int type) {
        ChatMessage msg = new ChatMessage();
        msg.setId(id);
        msg.setOrderId(BOOKING_ID);
        msg.setRole("student");
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
        SignalingMessage msg = makeSignal("offer", "1");

        videoRoomController.signal(BOOKING_ID, msg, makeAccessor(1));

        verify(messagingTemplate).convertAndSend("/topic/room/42/signal", msg);
    }

    @Test
    void signal_answer_shouldRelayToCorrectTopic() {
        SignalingMessage msg = makeSignal("answer", "2");

        videoRoomController.signal(BOOKING_ID, msg, makeAccessor(2));

        verify(messagingTemplate).convertAndSend("/topic/room/42/signal", msg);
    }

    @Test
    void signal_iceCandidate_shouldRelayToCorrectTopic() {
        SignalingMessage msg = makeSignal("candidate", "1");
        msg.setCandidate("candidate:...");
        msg.setSdpMid("0");
        msg.setSdpMLineIndex(0);

        videoRoomController.signal(BOOKING_ID, msg, makeAccessor(1));

        verify(messagingTemplate).convertAndSend("/topic/room/42/signal", msg);
    }

    @Test
    void signal_shouldNotInteractWithChatService() {
        videoRoomController.signal(BOOKING_ID, makeSignal("offer", "1"), makeAccessor(1));

        verifyNoInteractions(chatMessageService);
    }

    @Test
    void signal_differentBookingId_shouldUseDifferentTopic() {
        SignalingMessage msg = makeSignal("offer", "1");

        videoRoomController.signal(99L, msg, makeAccessor(1));

        verify(messagingTemplate).convertAndSend("/topic/room/99/signal", msg);
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/room/42/signal"), any(Object.class));
    }

    // ── chat() — 即時聊天訊息（文字）────────────────────────────────────────

    @Test
    void chat_textMessage_shouldSaveAndBroadcast() {
        ChatMessageRequest req = makeChatReq(MessageType.TEXT.getValue(), "Hello!", null);
        ChatMessage saved = makeSavedMsg(1L, MessageType.TEXT.getValue());
        when(chatMessageService.save(BOOKING_ID, "student", MessageType.TEXT.getValue(), "Hello!", null))
                .thenReturn(saved);

        videoRoomController.chat(BOOKING_ID, req, makeAccessor(1));

        verify(chatMessageService).save(BOOKING_ID, "student", MessageType.TEXT.getValue(), "Hello!", null);
        verify(messagingTemplate).convertAndSend("/topic/room/42/chat", saved);
    }

    @Test
    void chat_nullMessageType_shouldDefaultToText() {
        ChatMessageRequest req = makeChatReq(null, "Hello!", null);
        ChatMessage saved = makeSavedMsg(1L, MessageType.TEXT.getValue());
        when(chatMessageService.save(BOOKING_ID, "student", MessageType.TEXT.getValue(), "Hello!", null))
                .thenReturn(saved);

        videoRoomController.chat(BOOKING_ID, req, makeAccessor(1));

        verify(chatMessageService).save(BOOKING_ID, "student", MessageType.TEXT.getValue(), "Hello!", null);
    }

    @Test
    void chat_broadcastContainsPersistedEntity() {
        ChatMessageRequest req = makeChatReq(MessageType.TEXT.getValue(), "Hi", null);
        ChatMessage saved = makeSavedMsg(99L, MessageType.TEXT.getValue());
        when(chatMessageService.save(any(), any(), any(), any(), any())).thenReturn(saved);

        videoRoomController.chat(BOOKING_ID, req, makeAccessor(1));

        verify(messagingTemplate).convertAndSend(eq("/topic/room/42/chat"),
                argThat((ChatMessage msg) -> msg.getId().equals(99L)));
    }

    @Test
    void chat_shouldNotBroadcastToSignalOrEventsTopic() {
        ChatMessageRequest req = makeChatReq(MessageType.TEXT.getValue(), "Hi", null);
        when(chatMessageService.save(any(), any(), any(), any(), any())).thenReturn(makeSavedMsg(1L, 1));

        videoRoomController.chat(BOOKING_ID, req, makeAccessor(1));

        verify(messagingTemplate, never()).convertAndSend(eq("/topic/room/42/signal"), any(Object.class));
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/room/42/events"), any(Object.class));
    }

    // ── chat() — 即時聊天訊息（媒體）────────────────────────────────────────

    @Test
    void chat_stickerMessage_shouldSaveAndBroadcast() {
        ChatMessageRequest req = makeChatReq(MessageType.STICKER.getValue(), null, "stickers/001.png");
        req.setRole("2");
        ChatMessage saved = makeSavedMsg(2L, MessageType.STICKER.getValue());
        when(chatMessageService.save(BOOKING_ID, "tutor", MessageType.STICKER.getValue(), null, "stickers/001.png"))
                .thenReturn(saved);

        videoRoomController.chat(BOOKING_ID, req, makeAccessor(2));

        verify(chatMessageService).save(BOOKING_ID, "tutor", MessageType.STICKER.getValue(), null, "stickers/001.png");
        verify(messagingTemplate).convertAndSend("/topic/room/42/chat", saved);
    }

    @Test
    void chat_voiceMessage_shouldSaveAndBroadcast() {
        ChatMessageRequest req = makeChatReq(MessageType.VOICE.getValue(), null, "audio/001.mp3");
        req.setRole("2");
        ChatMessage saved = makeSavedMsg(3L, MessageType.VOICE.getValue());
        when(chatMessageService.save(BOOKING_ID, "tutor", MessageType.VOICE.getValue(), null, "audio/001.mp3"))
                .thenReturn(saved);

        videoRoomController.chat(BOOKING_ID, req, makeAccessor(2));

        verify(chatMessageService).save(BOOKING_ID, "tutor", MessageType.VOICE.getValue(), null, "audio/001.mp3");
    }

    @Test
    void chat_imageMessage_shouldSaveAndBroadcast() {
        ChatMessageRequest req = makeChatReq(MessageType.IMAGE.getValue(), null, "images/001.jpg");
        req.setRole("2");
        ChatMessage saved = makeSavedMsg(4L, MessageType.IMAGE.getValue());
        when(chatMessageService.save(BOOKING_ID, "tutor", MessageType.IMAGE.getValue(), null, "images/001.jpg"))
                .thenReturn(saved);

        videoRoomController.chat(BOOKING_ID, req, makeAccessor(2));

        verify(chatMessageService).save(BOOKING_ID, "tutor", MessageType.IMAGE.getValue(), null, "images/001.jpg");
    }

    @Test
    void chat_videoMessage_shouldSaveAndBroadcast() {
        ChatMessageRequest req = makeChatReq(MessageType.VIDEO.getValue(), null, "videos/001.mp4");
        req.setRole("2");
        ChatMessage saved = makeSavedMsg(5L, MessageType.VIDEO.getValue());
        when(chatMessageService.save(BOOKING_ID, "tutor", MessageType.VIDEO.getValue(), null, "videos/001.mp4"))
                .thenReturn(saved);

        videoRoomController.chat(BOOKING_ID, req, makeAccessor(2));

        verify(chatMessageService).save(BOOKING_ID, "tutor", MessageType.VIDEO.getValue(), null, "videos/001.mp4");
    }

    // ── event() — 房間加入/離開事件 ──────────────────────────────────────────

    @Test
    void event_joined_shouldBroadcastToCorrectTopic() {
        RoomEvent event = makeRoomEvent("joined", 1);

        videoRoomController.event(BOOKING_ID, event, makeAccessor(1));

        verify(messagingTemplate).convertAndSend("/topic/room/42/events", event);
    }

    @Test
    void event_left_shouldBroadcastToCorrectTopic() {
        RoomEvent event = makeRoomEvent("left", 2);

        videoRoomController.event(BOOKING_ID, event, makeAccessor(2));

        verify(messagingTemplate).convertAndSend("/topic/room/42/events", event);
    }

    @Test
    void event_shouldNotInteractWithChatService() {
        videoRoomController.event(BOOKING_ID, makeRoomEvent("joined", 1), makeAccessor(1));

        verifyNoInteractions(chatMessageService);
    }

    @Test
    void event_differentBookingId_shouldUseDifferentTopic() {
        RoomEvent event = makeRoomEvent("joined", 1);

        videoRoomController.event(100L, event, makeAccessor(1));

        verify(messagingTemplate).convertAndSend("/topic/room/100/events", event);
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/room/42/events"), any(Object.class));
    }
}
