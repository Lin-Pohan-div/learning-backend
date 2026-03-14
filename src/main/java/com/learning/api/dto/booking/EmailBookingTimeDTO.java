package com.learning.api.dto.booking;
import java.time.LocalDate;

import lombok.Data;

@Data
public class EmailBookingTimeDTO {

    private LocalDate date;
    private Integer hour;

    // getter setter
}