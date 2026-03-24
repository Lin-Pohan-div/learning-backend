package com.learning.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewDTO {
    private String studentName;
    private Integer rating;
    private String comment;
    private Instant updatedAt;
}
