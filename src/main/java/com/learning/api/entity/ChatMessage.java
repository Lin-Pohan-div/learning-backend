package com.learning.api.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(nullable = false)
    private Byte role;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Timestamp createdAt;
}