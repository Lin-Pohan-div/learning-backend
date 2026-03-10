package com.learning.api.dto;

import lombok.Data;

@Data
public class TutorProfileDTO {
    private Long tutorId;    // 老師的 ID
    private String name;     // 要更新到 users 表的名字
    private String intro;    // 要更新到 tutors 表的介紹
    private String certificate; // 證照
    private String video;    // 影片連結
}