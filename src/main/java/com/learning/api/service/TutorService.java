package com.learning.api.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learning.api.dto.TutorUpdateDTO;
import com.learning.api.entity.Course;
import com.learning.api.entity.Review;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.ReviewRepository; // Repo
import com.learning.api.repo.TutorRepo;
import com.learning.api.repo.TutorScheduleRepo;

@Service
public class TutorService {

    @Autowired
    private TutorRepo tutorRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private TutorScheduleRepo scheduleRepo;

    @Autowired
    private ReviewRepository reviewRepo;

    // ── 查詢功能 (Query) ──────────────────────────────────────────

    /** 取得老師實體物件 */
    public Tutor findTutorById(Long id) {
        return tutorRepo.findById(id).orElse(null);
    }

    /** 取得特定老師的課表排程，並排序 */
    public List<TutorSchedule> findSchedulesByTutorId(Long tutorId) {
        return scheduleRepo.findByTutorIdOrderByWeekdayAscHourAsc(tutorId);
    }

    /** 取得老師的所有課程 */
    public List<Course> findCoursesByTutorId(Long tutorId) {
        return courseRepo.findByTutorId(tutorId);
    }

    /** 取得單一課程資訊 */
    public Course findCourseById(Long courseId) {
        return courseRepo.findById(courseId).orElse(null);
    }

    /** 取得課程的評價列表 */
    public List<Review> findReviewsByCourseId(Long courseId) {
        return reviewRepo.findByCourseIdOrderByUpdatedAtDesc(courseId);
    }

    // ── 個人資料處理 (Profile Handling) ─────────────────────────────

    /** 將 Tutor 轉換為 DTO 用於編輯頁面 */
    public TutorUpdateDTO getProfileDTO(Long tutorId) {
        Tutor tutor = tutorRepo.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("找不到老師 id=" + tutorId));

        TutorUpdateDTO dto = new TutorUpdateDTO();
        dto.setAvatar(tutor.getAvatar());
        dto.setTitle(tutor.getTitle());
        dto.setIntro(tutor.getIntro());
        dto.setCertificate1(tutor.getCertificate1());
        dto.setCertificateName1(tutor.getCertificateName1());
        dto.setCertificate2(tutor.getCertificate2());
        dto.setCertificateName2(tutor.getCertificateName2());
        dto.setVideoUrl1(tutor.getVideoUrl1());
        dto.setVideoUrl2(tutor.getVideoUrl2());
        return dto;
    }

    /*
     * 完整更新個人資料（支援部分更新：DTO 欄位為 null 則不更新）
     */
    @Transactional
    public void updateProfile(Long tutorId, TutorUpdateDTO dto) {
        Tutor tutor = tutorRepo.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("找不到老師 id=" + tutorId));

        // 核心欄位更新
        if (dto.getAvatar() != null)
            tutor.setAvatar(dto.getAvatar());
        if (dto.getTitle() != null)
            tutor.setTitle(dto.getTitle());

        // 詳細資訊更新
        if (dto.getIntro() != null)
            tutor.setIntro(dto.getIntro());
        if (dto.getCertificate1() != null)
            tutor.setCertificate1(dto.getCertificate1());
        if (dto.getCertificateName1() != null)
            tutor.setCertificateName1(dto.getCertificateName1());
        if (dto.getCertificate2() != null)
            tutor.setCertificate2(dto.getCertificate2());
        if (dto.getCertificateName2() != null)
            tutor.setCertificateName2(dto.getCertificateName2());
        if (dto.getVideoUrl1() != null)
            tutor.setVideoUrl1(dto.getVideoUrl1());
        if (dto.getVideoUrl2() != null)
            tutor.setVideoUrl2(dto.getVideoUrl2());

        tutorRepo.save(tutor);
    }
}