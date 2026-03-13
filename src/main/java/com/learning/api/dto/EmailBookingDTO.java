package com.learning.api.dto;

import java.util.List;

import lombok.Data;

@Data
public class EmailBookingDTO {

    private String tutorEmail;
    private String tutorName;

    private String studentName;
    private String courseName;

    private List<EmailBookingTimeDTO> times;



    // getter setter
}