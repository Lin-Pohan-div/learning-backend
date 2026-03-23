package com.learning.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class BookingDTO {
    private Long id;
    private Long orderId;
    private Long tutorId;
    private Long studentId;
    private String studentName;
    private String courseName;
    private LocalDate date;
    private Integer hour;
    private Integer status;
    private Boolean slotLocked;
}