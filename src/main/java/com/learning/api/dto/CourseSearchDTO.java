package com.learning.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CourseSearchDTO {
    private Long id; // 課程 ID
    private Long tutorId; // 老師 ID（用於跳轉 /tutor/{tutorId}）
    private String teacherName;
    private String avatarUrl; // 新增：老師頭像
    private String title; // 新增：老師標題
    private String courseName;
    private Integer subject;
    private String description;
    private Integer price;
}
