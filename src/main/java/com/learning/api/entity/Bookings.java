package com.learning.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "bookings", uniqueConstraints = {
    @UniqueConstraint(name = "uq_booking_tutor_date_hour", columnNames = {"tutor_id", "date", "hour"})
})
@Getter
@Setter
public class Bookings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(name = "order_id")
    private Long orderId;
    
    @Column(name = "tutor_id", nullable = false)
    private Long tutorId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer hour;

    @Column(name = "slot_locked")
    private Boolean slotLocked;//請假OR退款SETNULL

    @Column(nullable = false)
    private Byte status;

    //@OneToOne(mappedBy = "lesson")
    //private LessonFeedback feedback;
}