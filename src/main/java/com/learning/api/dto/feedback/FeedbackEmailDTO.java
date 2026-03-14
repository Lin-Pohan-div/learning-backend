package com.learning.api.dto.feedback;
import java.time.LocalDate;

import lombok.Data;

@Data
public class FeedbackEmailDTO {

    private String studentEmail;

    private String studentName;

    private String tutorName;

    private String courseName;

    private LocalDate date;   

    private int hour;      // 14

    private int focusScore;

    private int comprehensionScore;

    private int confidenceScore;

    private String comment;
    /*{
    "studentEmail":"rogerhou20001@gmail.com",
    "studentName":"小明",
    "tutorName":"王老師",
    "courseName":"英文會話",
    "date":"2026-03-10",
    "hour":14,
    "focusScore":4,
    "comprehensionScore":3,
    "confidenceScore":5,
    "comment":"今天口語表達進步很多。"
  }*/

}