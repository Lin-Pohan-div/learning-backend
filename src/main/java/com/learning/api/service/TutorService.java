package com.learning.api.service;

import java.util.List;

import com.learning.api.dto.tutor.TutorUpdateReq;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.entity.User;
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

    public Tutor getTutor(Long id) {
        return tutorRepo.findById(id).orElse(null);
    }

    public List<TutorSchedule> findSchedulesByTutorId(Long id) {
        return tutorScheduleRepo.findByTutorId(id);
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
