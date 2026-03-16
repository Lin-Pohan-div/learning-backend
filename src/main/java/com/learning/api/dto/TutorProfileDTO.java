package com.learning.api.dto;

import java.util.List;

import com.learning.api.entity.Reviews;
import com.learning.api.entity.TutorSchedule;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TutorProfileDTO {
    private String name;
    private String headline;
    private String avatar;
    private String intro;
    private String certificate_name_1;
    private String videoUrl1;
    private List<TutorSchedule> schedules;
    private List<Reviews> reviews;
    private Double averageRating;
}
