package com.learning.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learning.api.entity.Course;
import com.learning.api.entity.Review;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.ReviewRepository;

import com.learning.api.repo.TutorRepo;
import com.learning.api.repo.TutorScheduleRepo;


@Service
public class TutorService {

    @Autowired
    private TutorRepo tutorRepo;
    
    @Autowired 
    private CourseRepo courseRepo; // 新增

    @Autowired
    private TutorScheduleRepo scheduleRepo;

    @Autowired
    private ReviewRepository reviewRepo;

    /**
     * 取得老師完整檔案（包含 User 基本資料）
     */
    public Tutor findTutorById(Long id) {
        return tutorRepo.findById(id).orElse(null);
    }

    /**
     * 取得特定老師的所有開放時段，並依照星期與小時排序
     */
    public List<TutorSchedule> findSchedulesByTutorId(Long tutorId) {
        return scheduleRepo.findByTutorIdOrderByWeekdayAscHourAsc(tutorId);
    }

    // 取得老師的所有課程
    public List<Course> findCoursesByTutorId(Long tutorId) {
        return courseRepo.findByTutorId(tutorId);
    }

    // 根據課程 ID 取得該課的評價
    public List<Review> findReviewsByCourseId(Long courseId) {
        return reviewRepo.findByCourseIdOrderByUpdatedAtDesc(courseId);
    }
    
    // 取得單一課程資訊
    public Course findCourseById(Long courseId) {
        return courseRepo.findById(courseId).orElse(null);
    }

    /**
     * 更新老師的標題與頭像（用於個人設定頁面）
     */
    @Transactional
    public void updateTutorProfile(Long id, String title, String avatar) {
        Tutor tutor = tutorRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("找不到該老師"));
        
        tutor.setTitle(title);
        tutor.setAvatar(avatar);
        tutorRepo.save(tutor);
    }
}

