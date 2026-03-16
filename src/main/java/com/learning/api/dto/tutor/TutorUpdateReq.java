package com.learning.api.dto.tutor;

import lombok.Data;

@Data
public class TutorUpdateReq {
    private Long tutorId;          // 老師的 ID（對應 users.id）

    // 更新到 users 表
    private String name;           // 老師姓名

    // 更新到 tutors 表
    private String title;          // 個人檔案標題
    private String avatar;         // 大頭照 URL
    private String intro;          // 個人簡介
    private String education;      // 最高學歷

    private String certificate1;     // 專業證照1位址
    private String certificateName1; // 專業證照欄名1
    private String certificate2;     // 專業證照2位址
    private String certificateName2; // 專業證照欄名2

    private String videoUrl1;      // 介紹影片連結
    private String videoUrl2;      // 試教影片連結

    private String bankCode;       // 收款銀行代碼（如 822）
    private String bankAccount;    // 收款銀行帳號
}
