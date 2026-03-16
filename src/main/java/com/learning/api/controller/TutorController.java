package com.learning.api.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learning.api.dto.TutorProfileDTO;
import com.learning.api.entity.Course;
import com.learning.api.entity.Review;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.service.TutorService;

@RestController
@RequestMapping("api/tutor")
@CrossOrigin(origins = "http://localhost:5173") // 允許前端開發環境跨域
public class TutorController {

    @Autowired
    private TutorService tutorService;

    /**
     * 以 API 形式取得老師個人檔案資料
     * @param id 老師的 ID
     */
    /* @GetMapping("/{id}")
    public ResponseEntity<?> getTutorProfile(
            @PathVariable Long id, 
            @RequestParam(required = false) Long courseId) { */
        
        // 1. 取得老師核心資料
        /* Tutor tutor = tutorService.findTutorById(id);
        
        if (tutor == null) {
            return ResponseEntity.notFound().build();
        } */

       /*  // 2. 取得老師的課表與課程列表
        List<TutorSchedule> schedules = tutorService.findSchedulesByTutorId(id);
        List<Course> courses = tutorService.findCoursesByTutorId(id); */
        
        // 3. 處理課程與評價邏輯
       /*  Course selectedCourse = null;
        if (courseId != null) {
            selectedCourse = tutorService.findCourseById(courseId);
        } else if (!courses.isEmpty()) {
            selectedCourse = courses.get(0);
        }

        List<Review> reviews = (selectedCourse != null) ? 
                               tutorService.findReviewsByCourseId(selectedCourse.getId()) : 
                               new ArrayList<>(); */

        // 4. 計算平均評分
       /*  double avgRating = reviews.stream()
                                  .mapToInt(Review::getRating)
                                  .average()
                                  .orElse(0.0); */

        // 5. 將結果封裝進 DTO (TutorProfileDTO)
        /* TutorProfileDTO dto = new TutorProfileDTO();
        dto.setName(tutor.getUser().getName()); // 假設 Tutor 關聯 User
        dto.setHeadline(tutor.getTitle());
        dto.setAvatar(tutor.getAvatar());
        dto.setIntro(tutor.getIntro());
        // 證照與影片 (根據 Tutor.java 的欄位名稱)
        dto.setCertificate_name_1(tutor.getCertificateName1());
        dto.setVideoUrl1(tutor.getVideoUrl1());

        // 列表與評分 (由 Service 取得的資料)
        dto.setSchedules(schedules);
        dto.setReviews(reviews);
        dto.setAverageRating(Double.parseDouble(String.format("%.1f", avgRating)));

        return ResponseEntity.ok(dto); */
    }
/* } */