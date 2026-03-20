package com.learning.api.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.Instant;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false, length = 255)
    private String role; // student / tutor

    @Column(name = "message_type", nullable = false)
    private Integer messageType = 1; // 1=text, 2=sticker

    @Column(length = 1000)
    private String message;

    @Column(name = "media_url", length = 500)
    private String mediaUrl;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}