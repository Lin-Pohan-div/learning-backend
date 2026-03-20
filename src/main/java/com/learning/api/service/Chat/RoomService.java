package com.learning.api.service.Chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.learning.api.dto.videoroom.RoomEvent;
import com.learning.api.dto.videoroom.RoomParticipant;
import com.learning.api.dto.videoroom.RoomStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 視訊教室房間管理 Service
 * 以 In-Memory 追蹤各 bookingId 的房間狀態與參與者
 */
@Slf4j
@Service
public class RoomService {

    // key = bookingId
    private final ConcurrentHashMap<Long, RoomStatus> rooms = new ConcurrentHashMap<>();

    // key = STOMP sessionId, value = bookingId（用於斷線反查）
    private final ConcurrentHashMap<String, Long> sessionToBooking = new ConcurrentHashMap<>();

    private final SimpMessagingTemplate messagingTemplate;

    public RoomService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // ─── 加入房間 ────────────────────────────────────────────

    public RoomStatus joinRoom(Long bookingId, Long userId, Integer role, String sessionId) {
        RoomStatus room = rooms.computeIfAbsent(bookingId, RoomStatus::new);

        // 同一 session 不重複加入
        boolean alreadyIn = room.getParticipants().stream()
                .anyMatch(p -> p.getSessionId().equals(sessionId));
        if (!alreadyIn) {
            room.getParticipants().add(new RoomParticipant(userId, role, sessionId, Instant.now()));
        }

        sessionToBooking.put(sessionId, bookingId);
        updateState(room);

        log.info("[RoomService] joinRoom bookingId={} userId={} role={} sessionId={} → state={}",
                bookingId, userId, role, sessionId, room.getState());
        return room;
    }

    // ─── 離開房間 ────────────────────────────────────────────

    public RoomStatus leaveRoom(Long bookingId, String sessionId) {
        RoomStatus room = rooms.get(bookingId);
        if (room == null) return null;

        room.getParticipants().removeIf(p -> p.getSessionId().equals(sessionId));
        sessionToBooking.remove(sessionId);

        if (room.getParticipants().isEmpty()) {
            rooms.remove(bookingId);
            log.info("[RoomService] leaveRoom bookingId={} → 房間已清除", bookingId);
            return room;
        }

        updateState(room);
        log.info("[RoomService] leaveRoom bookingId={} sessionId={} → state={}",
                bookingId, sessionId, room.getState());
        return room;
    }

    // ─── 查詢 ────────────────────────────────────────────────

    public Optional<RoomStatus> getRoomStatus(Long bookingId) {
        return Optional.ofNullable(rooms.get(bookingId));
    }

    public List<RoomParticipant> getParticipants(Long bookingId) {
        return getRoomStatus(bookingId)
                .map(r -> (List<RoomParticipant>) r.getParticipants())
                .orElse(List.of());
    }

    public boolean isRoomActive(Long bookingId) {
        return getRoomStatus(bookingId)
                .map(r -> "ACTIVE".equals(r.getState()))
                .orElse(false);
    }

    // ─── 斷線事件監聽 ────────────────────────────────────────

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        Long bookingId   = sessionToBooking.get(sessionId);
        if (bookingId == null) return;

        log.info("[RoomService] STOMP 斷線 sessionId={} bookingId={}", sessionId, bookingId);
        RoomStatus room = leaveRoom(bookingId, sessionId);

        // 廣播離開事件給房間內剩餘參與者
        if (room != null) {
            RoomEvent leaveEvent = new RoomEvent();
            leaveEvent.setType("left");
            messagingTemplate.convertAndSend(
                    "/topic/room/" + bookingId + "/events",
                    (Object) leaveEvent
            );
        }
    }

    // ─── 狀態更新輔助 ────────────────────────────────────────

    private void updateState(RoomStatus room) {
        List<RoomParticipant> ps = room.getParticipants();
        boolean hasStudent = ps.stream().anyMatch(p -> p.getRole() == 1);
        boolean hasTutor   = ps.stream().anyMatch(p -> p.getRole() == 2);

        if (hasStudent && hasTutor) {
            room.setState("ACTIVE");
        } else if (!ps.isEmpty()) {
            // 曾經 ACTIVE 後有人離開 → ENDED；否則保持 WAITING
            if ("ACTIVE".equals(room.getState())) {
                room.setState("ENDED");
            } else {
                room.setState("WAITING");
            }
        }
    }
}
