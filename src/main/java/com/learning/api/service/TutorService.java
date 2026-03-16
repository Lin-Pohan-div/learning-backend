package com.learning.api.service;

import com.learning.api.dto.tutor.TutorReq;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.User;
import com.learning.api.repo.TutorRepository;
import com.learning.api.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TutorService {

    @Autowired
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
}
