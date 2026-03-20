package com.learning.api.dto.ChatRoom;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private Long bookingId;
    private String role; // student / tutor
    private Integer messageType; // 1=text (default), 2=sticker, 3=voice, 4=image, 5=video
    private String message;
    private String mediaUrl;
}
