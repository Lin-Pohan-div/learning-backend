package com.learning.api.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class ScheduleDTO {

    @Getter
    @Setter
    public static class ToggleReq {
        private Long tutorId;
        private Integer weekday;
        private Integer hour;
        private Boolean isAvailable; // 對齊 DB: 想要變成開放 (true) 或 關閉 (false)
    }

    @Getter
    @Setter
    public static class Res {
        private Long id;
        private Integer weekday;
        private Integer hour;
        private Boolean isAvailable;

        public Res(Long id, Integer weekday, Integer hour, Boolean isAvailable) {
            this.id = id;
            this.weekday = weekday;
            this.hour = hour;
            this.isAvailable = isAvailable;
        }
    }

    // 🔥 批次操作
    @Getter
    @Setter
    public static class BatchToggleReq {
        private Long tutorId;
        private List<Slot> slots;
    }

    @Getter
    @Setter
    public static class Slot {
        private Integer weekday;
        private Integer hour;
        private Boolean isAvailable;
    }
}