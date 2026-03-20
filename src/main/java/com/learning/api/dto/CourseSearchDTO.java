package com.learning.api.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class CourseSearchDTO {
    private Long id;
    private Long tutorId;
    private String teacherName;
    private String avatarUrl;
    private String title;
    private String courseName;
    private Integer subject;
    private String description;
    private Integer price;

    // 🌟 新增：存放時段代碼的清單 (格式如: "1-morning")
    private List<String> availableSlots;
}