package com.learning.api.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class TodayCourseDto {
    private Long bookingId;
    private LocalDate date;    // 對應資料庫 date
    private Integer hour;         // 對應資料庫 hour (時段)
    private Integer status;       // 狀態 (1:排程中, 2:完成...)
    private String tutorname;      // 老師名字
}
