package com.learning.api.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TutorProfileDTO {
    private String name;
    private String headline; // 對應 tutor.title
    private String avatar;
    private String intro;

    // 證照（位址 + 名稱，兩張）
    private String certificate1;
    private String certificateName1;
    private String certificate2;
    private String certificateName2;

    // 影片（自我介紹 + 教學示範）
    private String videoUrl1;
    private String videoUrl2;

    private List<TutorScheduleDTO> schedules;
    private List<ReviewDTO> reviews;
    private Double averageRating;
}
