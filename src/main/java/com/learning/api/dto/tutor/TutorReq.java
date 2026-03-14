package com.learning.api.dto.tutor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TutorReq {
    private Long tutorId;           // POST 時使用，對應 users.id (role=2)
    private String title;           // 個人檔案標題
    private String avatarUrl;       // 大頭照 URL
    private String intro;           // 個人簡介
    private String education;       // 最高學歷
    private String certificate1;    // 專業證照1位址
    private String certificateName1;// 專業證照欄名1
    private String certificate2;    // 專業證照2位址
    private String certificateName2;// 專業證照欄名2
    private String videoUrl1;       // 介紹影片連結
    private String videoUrl2;       // 試教影片連結
    private String bankCode;        // 收款銀行代碼 (如 822)
    private String bankAccount;     // 收款銀行帳號
}
