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

    @Column(nullable = false)
    private Integer role; //1Student 2tutor

    @Column(name = "message_type", nullable = false)
    private Integer messageType = 1; // 1=text, 2=sticker

    // allow null in JPA mapping but some existing schema may still mark this NOT NULL
    // we provide a default empty string to avoid persistence errors when media only
    @Column(length = 1000, nullable = true)
    private String message = "";

    @Column(name = "media_url", length = 500)
    private String mediaUrl;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}