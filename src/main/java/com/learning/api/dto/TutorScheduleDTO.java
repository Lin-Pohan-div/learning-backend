package com.learning.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TutorScheduleDTO {
    private Integer weekday;
    private Integer hour;
}
