package com.learning.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tutor_schedules")
@Getter
@Setter
public class TutorSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tutor_id", nullable = false)
    private Long tutorId;

    @Column(nullable = false)
    private Integer weekday;

    @Column(nullable = false)
    private Integer hour;

    @Column(length = 20)
    private String status;
}