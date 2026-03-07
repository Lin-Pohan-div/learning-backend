package com.learning.api.dto;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private Long bookingId;
    private Byte role;
    private String message;
}
