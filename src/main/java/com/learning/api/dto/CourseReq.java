package com.learning.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseReq {
    private String name; // 課程名稱
    private Integer subject; // 科目代碼 11~31
    private String description; // 課程介紹
    private Integer price; // 單堂價格
    private Boolean active; // true=上架 / false=下架
}
