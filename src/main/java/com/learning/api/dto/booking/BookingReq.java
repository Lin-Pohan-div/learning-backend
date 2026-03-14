package com.learning.api.dto.booking;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingReq {
    private Long userId;
    private Long courseId;
    private Integer lessonCount;
}
