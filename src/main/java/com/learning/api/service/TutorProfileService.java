package com.learning.api.service;

import com.learning.api.dto.TutorProfileDTO;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.User;
import com.learning.api.repo.TutorRepo;
import com.learning.api.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TutorProfileService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TutorRepo tutorRepo;

    // 更新老師個人檔案
    @Transactional // 確保兩張表的更新同時成功或失敗
    public boolean updateProfile(TutorProfileDTO dto) {

        // 1. 先去 users 表找出這個人，更新他的名字
        User user = userRepo.findById(dto.getTutorId()).orElse(null);
        if (user == null) {
            System.out.println("❌ 找不到該名老師！");
            return false;
        }

        // 如果前端有傳名字來，才更新名字
        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            user.setName(dto.getName());
            userRepo.save(user);
        }

        // 2. 去 tutors 表更新專屬資料 (如果原本沒有，就 new 一個新的)
        Tutor tutor = tutorRepo.findById(dto.getTutorId()).orElse(new Tutor());
        tutor.setId(dto.getTutorId()); // 綁定 ID
        tutor.setIntro(dto.getIntro());
        tutor.setCertificate(dto.getCertificate());
        tutor.setVideoUrl1(dto.getVideo());

        tutorRepo.save(tutor);

        System.out.println("✅ 老師檔案更新成功！");
        return true;
    }
}