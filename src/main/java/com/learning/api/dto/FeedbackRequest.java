package com.learning.api.dto;

import lombok.Data;

@Data
public class FeedbackRequest {
    private Long bookingId;
    private Byte rating;
    private String comment;
}
