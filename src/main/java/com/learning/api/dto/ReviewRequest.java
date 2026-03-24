package com.learning.api.dto;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long userId;
    private Long courseId;
    private Integer rating;
    private String comment;

/*{
    "userId": 1,
    "courseId": 2,
    "focusScore": 88,
    "comprehensionScore": 92,
    "confidenceScore": 85,
    "comment": "課程內容豐富，老師教學耐心，非常推薦！"
  }*/

    // getter setter
}
