package com.learning.api.entity;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lesson_feedback")
@Getter
@Setter
public class LessonFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @JoinColumn(name = "booking_id")
    private Long bookId;

    @Column(name="focus_score" ,nullable = false)
    private Integer focusScore;

    @Column(name="comprehension_score" ,nullable = false)
    private Integer comprehensionScore;

    @Column(name="confidence_score" ,nullable = false)
    private Integer confidenceScore;

    @Column(nullable = true, length = 1000)
    private String comment; 

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
    

}