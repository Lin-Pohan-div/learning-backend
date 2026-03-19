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

import com.learning.api.dto.ReviewDTO;
import com.learning.api.dto.TutorProfileDTO;
import com.learning.api.dto.TutorScheduleDTO;
import com.learning.api.entity.Course;
import com.learning.api.entity.Review;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.service.TutorService;

@RestController
@RequestMapping("api/view/tutor")
@CrossOrigin(origins = "http://localhost:5173")
public class TutorController {

    @Autowired
    private TutorService tutorService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getTutorProfile(
            @PathVariable Long id,
            @RequestParam(required = false) Long courseId) {

        // 1. 取得老師核心資料
        Tutor tutor = tutorService.findTutorById(id);
        if (tutor == null) {
            return ResponseEntity.notFound().build();
        }

        // 2. 取得課表與課程列表
        List<TutorSchedule> schedules = tutorService.findSchedulesByTutorId(id);
        List<Course> courses = tutorService.findCoursesByTutorId(id);

        // 3. 決定顯示哪堂課的評價
        Course selectedCourse = null;
        if (courseId != null) {
            selectedCourse = tutorService.findCourseById(courseId);
        } else if (!courses.isEmpty()) {
            selectedCourse = courses.get(0);
        }

        List<Review> reviews = (selectedCourse != null)
                ? tutorService.findReviewsByCourseId(selectedCourse.getId())
                : new ArrayList<>();

        // 4. 計算平均評分
        double avgRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // 5. 組裝 DTO
        TutorProfileDTO dto = new TutorProfileDTO();

        // 基本資料
        dto.setName(tutor.getUser().getName());
        dto.setHeadline(tutor.getTitle());
        dto.setAvatar(tutor.getAvatar());
        dto.setIntro(tutor.getIntro());

        // 證照
        dto.setCertificate1(tutor.getCertificate1());
        dto.setCertificateName1(tutor.getCertificateName1());
        dto.setCertificate2(tutor.getCertificate2());
        dto.setCertificateName2(tutor.getCertificateName2());

        // 影片
        dto.setVideoUrl1(tutor.getVideoUrl1());
        dto.setVideoUrl2(tutor.getVideoUrl2());

        // 課表：Entity → DTO，避免 LAZY 與循環引用
        List<TutorScheduleDTO> scheduleDTOs = schedules.stream()
                .map(s -> new TutorScheduleDTO(s.getWeekday(), s.getHour()))
                .toList();
        dto.setSchedules(scheduleDTOs);

        // 評價：Entity → DTO，避免 LAZY 與敏感資料外洩
        List<ReviewDTO> reviewDTOs = reviews.stream()
                .map(r -> new ReviewDTO(
                        r.getStudent().getName(),
                        r.getRating(),
                        r.getComment(),
                        r.getUpdatedAt()))
                .toList();
        dto.setReviews(reviewDTOs);

        dto.setAverageRating(Double.parseDouble(String.format("%.1f", avgRating)));

        return ResponseEntity.ok(dto);
    }
}