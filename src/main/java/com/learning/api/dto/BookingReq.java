package com.learning.api.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

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
        // 學生 ID
        private Long userId;
        // 課程 ID
        private Long courseId;
        // 預約日期
        private LocalDate date;
        // 預約小時
        private Integer hour;
        // 訂單 ID (由結帳邏輯產生)
        private Long orderId;
        // 為了相容隊友邏輯保留的欄位
        private Integer lessonCount;
}
