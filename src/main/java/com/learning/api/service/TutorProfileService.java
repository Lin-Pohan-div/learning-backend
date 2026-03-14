package com.learning.api.service;

import com.learning.api.dto.tutor.TutorProfileDTO;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.User;
import com.learning.api.repo.TutorRepository;
import com.learning.api.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TutorProfileService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private TutorRepository tutorRepo;

    // [GET] 取得老師個人檔案
    public Tutor getProfile(Long tutorId) {
        return tutorRepo.findById(tutorId).orElse(null);
    }

    // [POST] 建立老師個人檔案（初次設定）
    @Transactional
    public String createProfile(TutorProfileDTO dto) {
        if (dto.getTutorId() == null) return "必須提供老師 ID";

        User user = userRepo.findById(dto.getTutorId()).orElse(null);
        if (user == null) return "找不到該名老師";

        if (tutorRepo.existsById(dto.getTutorId())) return "個人檔案已存在，請使用 PUT 更新";

        Tutor tutor = new Tutor();
        applyDtoToTutor(dto, tutor);
        tutorRepo.save(tutor);

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            user.setName(dto.getName());
            userRepo.save(user);
        }

        System.out.println("✅ 老師個人檔案建立成功！tutorId=" + dto.getTutorId());
        return "success";
    }

    // [PUT] 更新老師個人檔案
    @Transactional
    public String updateProfile(TutorProfileDTO dto) {
        if (dto.getTutorId() == null) return "必須提供老師 ID";

        User user = userRepo.findById(dto.getTutorId()).orElse(null);
        if (user == null) return "找不到該名老師";

        Tutor tutor = tutorRepo.findById(dto.getTutorId()).orElse(new Tutor());
        tutor.setId(dto.getTutorId());
        applyDtoToTutor(dto, tutor);
        tutorRepo.save(tutor);

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            user.setName(dto.getName());
            userRepo.save(user);
        }

        System.out.println("✅ 老師個人檔案更新成功！tutorId=" + dto.getTutorId());
        return "success";
    }

    // [DELETE] 刪除老師個人檔案
    public String deleteProfile(Long tutorId) {
        if (!tutorRepo.existsById(tutorId)) return "找不到該名老師的個人檔案";

        tutorRepo.deleteById(tutorId);
        System.out.println("✅ 老師個人檔案已刪除！tutorId=" + tutorId);
        return "success";
    }

    // 共用：將 DTO 欄位套用至 Tutor entity
    private void applyDtoToTutor(TutorProfileDTO dto, Tutor tutor) {
        tutor.setId(dto.getTutorId());
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
