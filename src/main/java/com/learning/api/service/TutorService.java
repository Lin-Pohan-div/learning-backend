package com.learning.api.service;

import java.util.List;

import com.learning.api.dto.tutor.TutorUpdateReq;
import com.learning.api.dto.tutor.TutorReq;
import com.learning.api.entity.Course;
import com.learning.api.entity.Reviews;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.entity.User;
import com.learning.api.enums.UserRole;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.ReviewRepository;
import com.learning.api.repo.TutorRepository;
import com.learning.api.repo.TutorScheduleRepo;
import com.learning.api.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TutorService {

    private final TutorRepository tutorRepo;
    private final UserRepository userRepo;
    private final TutorScheduleRepo tutorScheduleRepo;
    private final CourseRepo courseRepo;
    private final ReviewRepository reviewRepository;

    public Tutor getTutor(Long id) {
        return tutorRepo.findById(id).orElse(null);
    }

    public List<TutorSchedule> findSchedulesByTutorId(Long id) {
        return tutorScheduleRepo.findByTutorId(id);
    }

    public List<Course> findCoursesByTutorId(Long id) {
        return courseRepo.findByTutorId(id);
    }

    public Course findCourseById(Long id) {
        return courseRepo.findById(id).orElse(null);
    }

    public List<Reviews> findReviewsByCourseId(Long id) {
        return reviewRepository.findByCourseId(id);
    }

    public boolean createTutor(TutorReq req) {
        if (req == null || req.getTutorId() == null) return false;

        User user = userRepo.findById(req.getTutorId()).orElse(null);
        if (user == null) return false;

        if (user.getRole() != UserRole.TUTOR) return false;

        if (tutorRepo.existsById(req.getTutorId())) return false;

        Tutor tutor = new Tutor();
        tutor.setUser(user);
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

    // ── Profile lifecycle (merged from TutorProfileService) ──────────────────

    @Transactional
    public String createProfile(TutorUpdateReq dto) {
        if (dto.getTutorId() == null) return "必須提供老師 ID";

        User user = userRepo.findById(dto.getTutorId()).orElse(null);
        if (user == null) return "找不到該名老師";

        if (tutorRepo.existsById(dto.getTutorId())) return "個人檔案已存在，請使用 PUT 更新";

        Tutor tutor = new Tutor();
        tutor.setUser(user);
        applyDtoToTutor(dto, tutor);
        tutorRepo.save(tutor);

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            user.setName(dto.getName());
            userRepo.save(user);
        }
        return "success";
    }

    @Transactional
    public String updateProfile(TutorUpdateReq dto) {
        if (dto.getTutorId() == null) return "必須提供老師 ID";

        User user = userRepo.findById(dto.getTutorId()).orElse(null);
        if (user == null) return "找不到該名老師";

        Tutor tutor = tutorRepo.findById(dto.getTutorId()).orElse(new Tutor());
        tutor.setUser(user);
        applyDtoToTutor(dto, tutor);
        tutorRepo.save(tutor);

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            user.setName(dto.getName());
            userRepo.save(user);
        }
        return "success";
    }

    public String deleteProfile(Long tutorId) {
        if (!tutorRepo.existsById(tutorId)) return "找不到該名老師的個人檔案";
        tutorRepo.deleteById(tutorId);
        return "success";
    }

    private void applyDtoToTutor(TutorUpdateReq dto, Tutor tutor) {
        tutor.setTitle(dto.getTitle());
        tutor.setAvatarUrl(dto.getAvatar());
        tutor.setIntro(dto.getIntro());
        tutor.setEducation(dto.getEducation());
        tutor.setCertificate1(dto.getCertificate1());
        tutor.setCertificateName1(dto.getCertificateName1());
        tutor.setCertificate2(dto.getCertificate2());
        tutor.setCertificateName2(dto.getCertificateName2());
        tutor.setVideoUrl1(dto.getVideoUrl1());
        tutor.setVideoUrl2(dto.getVideoUrl2());
        tutor.setBankCode(dto.getBankCode());
        tutor.setBankAccount(dto.getBankAccount());
    }
}
