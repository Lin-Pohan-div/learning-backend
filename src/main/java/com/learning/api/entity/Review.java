package com.learning.api.entity;
import java.sql.Timestamp;
import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reviews")
@Getter
@Setter
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = true, length = 1000)
    private String comment;

    @Column(name = "updated_at", nullable = true,  insertable = false, updatable = false)
    private Instant updatedAt;
}