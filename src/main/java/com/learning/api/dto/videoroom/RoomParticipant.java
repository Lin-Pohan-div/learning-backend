package com.learning.api.dto.videoroom;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

/**
 * 視訊教室參與者資訊
 */
@Data
@AllArgsConstructor
public class RoomParticipant {
    private Long userId;
    private Integer role;      // 1=學生, 2=導師
    private String sessionId;  // STOMP session ID（用於斷線追蹤）
    private Instant joinedAt;
}
