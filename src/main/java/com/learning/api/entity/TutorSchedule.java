package com.learning.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tutor_schedules",
       uniqueConstraints = {@UniqueConstraint(columnNames = {"tutor_id", "weekday", "hour"})})
@Getter
@Setter
public class TutorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor; // 關聯 Tutor（不直接關聯 User）

    @Column(nullable = false)
    private Integer weekday; // 1-7 (星期一到星期日)

    @Column(nullable = false)
    private Integer hour; // 9-21 (開放時段)

    // 👉 修正：對齊 DB 的 is_available
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;
}
