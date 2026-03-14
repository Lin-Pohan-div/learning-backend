package com.learning.api.dto.course;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseReq {
    /*
    {
      "tutorId": 2,
      "name": "零基礎 Python 入門",
      "subject": 31,
      "description": "本課程適合初學者",
      "price": 700,
      "active": true
    }
    科目代碼：11低年級 12中年級 13高年級 21GEPT 22YLE 23國中先修 31其他
    tutorId 僅供開發測試使用，正式版應改由登入資訊取得
    */

    private Long tutorId;
    private String name;
    private Integer subject;
    private Integer level;
    private String description;
    private Integer price;
    private Boolean active;
}
