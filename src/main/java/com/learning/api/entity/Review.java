package com.learning.api.entity;
import java.time.Instant;

import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User student; // 撰寫評價的學生

    @Column(name = "course_id", nullable = false)
    private Long courseId; // 對應的課程 ID

    @Column(nullable = false)
    private Integer rating; // 評分 1–5 分

    @Column(nullable = true, length = 1000)
    private String comment; // 評論內容

    @Column(name = "updated_at", nullable = true,  insertable = false, updatable = false)
    private Instant updatedAt;
}