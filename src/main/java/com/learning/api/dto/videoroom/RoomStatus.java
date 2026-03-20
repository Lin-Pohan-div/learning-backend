package com.learning.api.dto.videoroom;

import lombok.Data;

import java.time.Instant;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 視訊教室狀態
 * state: WAITING（等待雙方）| ACTIVE（雙方皆在）| ENDED（有人離開）
 */
@Data
public class RoomStatus {
    private Long bookingId;
    private String state;  // WAITING | ACTIVE | ENDED
    private CopyOnWriteArrayList<RoomParticipant> participants;
    private Instant createdAt;

    public RoomStatus(Long bookingId) {
        this.bookingId    = bookingId;
        this.state        = "WAITING";
        this.participants = new CopyOnWriteArrayList<>();
        this.createdAt    = Instant.now();
    }
}
