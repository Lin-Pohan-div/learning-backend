package com.learning.api.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
public class CourseResp {
    private Long id;
    private Long tutorId;
    private String name;
    private Integer subject;
    private Integer level;
    private String description;
    private Integer price;
    private Boolean active;
    private Double avgRating;
    private List<FeedbackItem> feedbacks;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class FeedbackItem {
        private Integer rating;
        private String comment;
    }
}
