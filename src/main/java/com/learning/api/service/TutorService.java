package com.learning.api.service;

import com.learning.api.dto.tutor.TutorReq;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.User;
import com.learning.api.repo.TutorRepository;
import com.learning.api.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learning.api.entity.Course;
import com.learning.api.entity.Review;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.ReviewRepo;
import com.learning.api.repo.TutorRepo;
import com.learning.api.repo.TutorScheduleRepo;


@Service
public class TutorService {

    @Autowired
<<<<<<< HEAD
    private TutorRepository tutorRepo;

    @Autowired
    private UserRepository userRepo;

    public Tutor getTutor(Long id) {
        return tutorRepo.findById(id).orElse(null);
    }

    public boolean createTutor(TutorReq req) {
        if (req == null || req.getTutorId() == null) return false;

        User user = userRepo.findById(req.getTutorId()).orElse(null);
        if (user == null) return false;

        if (user.getRole() != 2) return false;

        if (tutorRepo.existsById(req.getTutorId())) return false;

        Tutor tutor = new Tutor();
        tutor.setId(req.getTutorId());
        applyFields(tutor, req);
        tutorRepo.save(tutor);
        return true;
    }

    public boolean updateTutor(Long id, TutorReq req) {
        if (req == null) return false;

        Tutor tutor = tutorRepo.findById(id).orElse(null);
        if (tutor == null) return false;

        applyFields(tutor, req);
        tutorRepo.save(tutor);
        return true;
    }

    public boolean deleteTutor(Long id) {
        if (!tutorRepo.existsById(id)) return false;
        tutorRepo.deleteById(id);
        return true;
    }

    private void applyFields(Tutor tutor, TutorReq req) {
        if (req.getTitle() != null) tutor.setTitle(req.getTitle());
        if (req.getAvatarUrl() != null) tutor.setAvatarUrl(req.getAvatarUrl());
        if (req.getIntro() != null) tutor.setIntro(req.getIntro());
        if (req.getEducation() != null) tutor.setEducation(req.getEducation());
        if (req.getCertificate1() != null) tutor.setCertificate1(req.getCertificate1());
        if (req.getCertificateName1() != null) tutor.setCertificateName1(req.getCertificateName1());
        if (req.getCertificate2() != null) tutor.setCertificate2(req.getCertificate2());
        if (req.getCertificateName2() != null) tutor.setCertificateName2(req.getCertificateName2());
        if (req.getVideoUrl1() != null) tutor.setVideoUrl1(req.getVideoUrl1());
        if (req.getVideoUrl2() != null) tutor.setVideoUrl2(req.getVideoUrl2());
        if (req.getBankCode() != null) tutor.setBankCode(req.getBankCode());
        if (req.getBankAccount() != null) tutor.setBankAccount(req.getBankAccount());
    }
}
=======
    private TutorRepo tutorRepo;
    
    @Autowired 
    private CourseRepo courseRepo; // 新增

    @Autowired
    private TutorScheduleRepo scheduleRepo;

    @Autowired
    private ReviewRepo reviewRepo;

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

>>>>>>> upstream/feature/view
