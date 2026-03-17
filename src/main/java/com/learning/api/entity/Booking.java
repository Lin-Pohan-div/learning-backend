package com.learning.api.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "tutor_id", nullable = false)
    private Long tutorId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer hour;

    @Column(nullable = false)
    private Integer status; //1 scheduled 2completed 3cancelled

    @Column(name = "slot_locked")
    private Boolean slotLocked;//請假OR退款SETNULL

}