package com.learning.api.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CheckoutReq {
    private Long studentId;
    private Long courseId;
    private Integer lessonCount;
    private List<Slot> selectedSlots; // 學生選的多個時段

    @Getter
    @Setter
    public static class Slot {
        private LocalDate date;
        private Integer hour;
    }
}