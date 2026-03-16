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

    // 老師的開放課表設定
    // 狀態：'available' (開放常態預約), 'inactive' (老師暫停開放此時段)
    @Column(nullable = false, length = 20)
    private String status = "available";
}
