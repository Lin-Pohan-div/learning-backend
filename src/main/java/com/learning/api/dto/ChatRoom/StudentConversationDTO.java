package com.learning.api.dto.ChatRoom;

import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
public class StudentConversationDTO {
    private Long orderId;
    private Long bookingRecordId;
    private List<Long> bookingIds;
    private List<Long> bookingRecordIds;
    private Long participantId;
    private String participantName;
    private String avatar;
    private String subject;
    private String lastMessage;
    private Instant lastMessageTime;
    private int unread;
}
