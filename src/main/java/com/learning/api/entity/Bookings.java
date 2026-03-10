package com.learning.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Bookings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    @Column(name = "discount_price", nullable = false)
    private Integer discountPrice;

    @Column(name = "lesson_count", nullable = false)
    private Integer lessonCount;

    @Column(name = "tutor_id", nullable = false)
    private Long tutorId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Date date;

    @Column(nullable = false)
    private Byte hour;

    @Column(name = "slot_locked")
    private Byte slotLocked;

    @Column(nullable = false)
    private Byte status;

    //@OneToOne(mappedBy = "lesson")
    //private LessonFeedback feedback;
}