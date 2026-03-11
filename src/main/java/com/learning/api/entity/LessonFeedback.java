package com.learning.api.entity;

import jakarta.persistence.*;
import java.time.Instant;
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

    
    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name="focus_score" ,nullable = false)
    private Integer focusScore;

    @Column(name="comprehension_score" ,nullable = false)
    private Integer comprehensionScore;

    @Column(name="confidence_score" ,nullable = false)
    private Integer confidenceScore;

    @Column(nullable = true, length = 1000)
    private String comment; 

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
    

}