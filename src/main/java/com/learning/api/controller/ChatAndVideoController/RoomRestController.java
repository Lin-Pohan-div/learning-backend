package com.learning.api.controller.ChatAndVideoController;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learning.api.service.Chat.RoomService;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 視訊教室房間狀態 REST API
 */
@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class RoomRestController {

    private final RoomService roomService;

    /**
     * 取得目前房間狀態與參與者列表
     * GET /api/room/{bookingId}/participants
     */
    @GetMapping("/{bookingId}/participants")
    public ResponseEntity<?> getParticipants(@PathVariable Long bookingId) {
        return roomService.getRoomStatus(bookingId)
                .map(status -> ResponseEntity.ok((Object) status))
                .orElseGet(() -> ResponseEntity.ok(Map.of(
                        "bookingId", bookingId,
                        "state", "NOT_STARTED",
                        "participants", List.of(),
                        "createdAt", Instant.now().toString()
                )));
    }
}
