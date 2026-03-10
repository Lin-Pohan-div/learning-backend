package com.learning.api.entity;

import jakarta.persistence.*;
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

    @Column(nullable = false)
    private Byte rating;

    @Column(nullable = true, length = 1000)
    private String comment;
    

}