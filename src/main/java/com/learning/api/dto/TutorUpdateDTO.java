package com.learning.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TutorUpdateDTO {
    private String avatar;
    private String title;
    private String intro;
    private String certificate1;
    private String certificateName1;
    private String certificate2;
    private String certificateName2;
    private String videoUrl1;
    private String videoUrl2;
}
