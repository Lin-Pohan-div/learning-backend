package com.learning.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "reviews")
@Getter
@Setter
public class Reviews {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "focus_score", nullable = false)
    private Integer focusScore;

    @Column(name = "comprehension_score", nullable = false)
    private Integer comprehensionScore;

    @Column(name = "confidence_score", nullable = false)
    private Integer confidenceScore;

    @Column(nullable = false)
    private Integer rating; // 評分 1–5 分

    @Column(nullable = true, length = 1000)
    private String comment;
}
