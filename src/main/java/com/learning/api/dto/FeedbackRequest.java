package com.learning.api.dto;

import lombok.Data;

@Data
public class FeedbackRequest {
    private Long bookingId;
    private Integer focusScore;
    private Integer comprehensionScore;
    private Integer confidenceScore;
    private Integer rating;
    private String comment;
}
