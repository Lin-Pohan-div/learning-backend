package com.learning.api.dto.booking;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingReq {
    private Long userId;
    private Long courseId;
    private Long orderId;
    private Integer lessonCount;
    private LocalDate date;
    private Integer hour;
}
