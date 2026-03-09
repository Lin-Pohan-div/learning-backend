package com.learning.api.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingReq {

    /*
    {
      "user_id": 1,  // 之後補 token 後拿掉
      "course_id": 5,
      "lesson_count": 10
    }
     */
    // 之後處理完 JWT userId 要拿掉
    private Long userId;

    private Long courseId;

    @Min(1)
    private Integer lessonCount;
}
