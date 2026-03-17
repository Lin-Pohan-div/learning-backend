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

/*{
    "tutorName": "王老師",
    "tutorEmail": "rogerhou20001@gmail.com",
    "studentName": "學生1",
    "courseName": "英文會話",
    "times": [
      {
        "date": "2026-03-10",
        "hour": 14
      },
      {
        "date": "2026-03-11",
        "hour": 15
      }
    ]
  }*/

    // getter setter
}