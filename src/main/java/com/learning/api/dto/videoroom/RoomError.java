package com.learning.api.dto.videoroom;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

/**
 * 視訊教室錯誤訊息 DTO
 * 廣播至 /topic/room/{bookingId}/errors
 */
@Data
@AllArgsConstructor
public class RoomError {
    private String code;     // BOOKING_NOT_FOUND | BOOKING_CANCELLED | INVALID_ROLE | *_ERROR
    private String message;
    private Instant timestamp;
}
