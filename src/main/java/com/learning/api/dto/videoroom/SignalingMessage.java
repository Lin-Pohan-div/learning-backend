package com.learning.api.dto.videoroom;

import lombok.Data;

/**
 * WebRTC 信令訊息 DTO
 * type: "offer" | "answer" | "candidate"
 */
@Data
public class SignalingMessage {
    private String type;          // offer / answer / candidate
    private String senderRole;    // student / tutor
    private String sdp;           // SDP (offer / answer 使用)
    private String candidate;     // ICE candidate 字串
    private String sdpMid;        // ICE candidate sdpMid
    private Integer sdpMLineIndex; // ICE candidate sdpMLineIndex
}
