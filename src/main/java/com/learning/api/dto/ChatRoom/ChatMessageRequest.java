package com.learning.api.dto.ChatRoom;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private Long bookingId;
    private String role; // student / tutor
    private Integer messageType; // 1=text (default), 2=sticker, 3=voice, 4=image, 5=video
    private String message;
    private String mediaUrl;

/*{
    "bookingId": 1,
    "role": 1,
    "messageType": 1,
    "message": "你好，請問今天的課程幾點開始？",
    "mediaUrl": "https://drive.google.com/file/d/1UCHd6M_Z4-ZnJPp6RTry15iZiMyH3tMP/view?usp=drive_link"
  }*/

    // getter setter
}
