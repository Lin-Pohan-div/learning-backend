package com.learning.api.dto;

import lombok.Getter;
import lombok.Setter;

public class ScheduleDTO {

    // 接收前端請求的 DTO
    @Getter
    @Setter
    public static class ToggleReq {
        private Long tutorId;       // 老師 ID
        private Integer weekday;    // 星期幾 (1-7)
        private Integer hour;       // 小時 (9-21)
        private String targetStatus;// 想要變成的狀態 ('available' 或 'inactive')
    }

    // 回傳給前端的 DTO (讓前端畫出綠色或灰色的格子)
    @Getter
    @Setter
    public static class Res {
        private Long id;
        private Integer weekday;
        private Integer hour;
        private String status;

        public Res(Long id, Integer weekday, Integer hour, String status) {
            this.id = id;
            this.weekday = weekday;
            this.hour = hour;
            this.status = status;
        }
    }
}