package com.learning.api.service;

import com.learning.api.entity.Course;
import com.learning.api.repo.CourseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeacherCourseService {

    @Autowired
    private CourseRepo courseRepo;

    // 老師新增課程的商業邏輯
    public boolean addCourse(Course course) {
        // 1. 防呆檢查：有沒有漏填必填欄位
        if (course.getTutorId() == null ||
                course.getSubject() == null ||
                course.getLevel() == null ||
                course.getPrice() == null) {
            System.out.println("❌ 開課失敗：有必填欄位沒有填寫！");
            return false;
        }

        // 2. 商業邏輯檢查：價格不能小於 0
        if (course.getPrice() <= 0) {
            System.out.println("❌ 開課失敗：課程價格必須大於 0 元！");
            return false;
        }

        // 3. 確保留言介紹不會超出資料庫限制
        if (course.getDescription() != null && course.getDescription().length() > 1000) {
            System.out.println("❌ 開課失敗：課程介紹太長了 (超過 1000 字)！");
            return false;
        }

        // 4. 存入資料庫
        courseRepo.save(course);
        System.out.println("✅ 開課成功！課程已存入資料庫。");
        return true;
    }
}