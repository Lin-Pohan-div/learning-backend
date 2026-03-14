package com.learning.api.dto.feedback;

import lombok.Data;

@Data
public class FeedbackRequest {
    private Long bookingId;
    private Integer focusScore;
    private Integer comprehensionScore;
    private Integer confidenceScore;
    private Integer rating;
    private String comment;

/*{
    "bookingId": 1,
    "focusScore": 85,
    "comprehensionScore": 90,
    "confidenceScore": 80,
    "rating": 5,
    "comment": "學生今天表現很好，專注力高，理解速度快。"
  }*/

    // getter setter
}
