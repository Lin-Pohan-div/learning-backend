package com.learning.api.dto;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long userId;
    private Long courseId;
    private Integer focusScore;
    private Integer comprehensionScore;
    private Integer confidenceScore;
    private String comment;
}
