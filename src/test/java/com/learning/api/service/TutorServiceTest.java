package com.learning.api.service;

import com.learning.api.dto.tutor.TutorReq;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.User;
import com.learning.api.repo.TutorRepository;
import com.learning.api.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TutorServiceTest {

    @Autowired
    private TutorService tutorService;

    @Autowired
    private TutorRepository tutorRepo;

    @Autowired
    private UserRepository userRepo;

    private Long tutorUserId;    // role=2, 已建立 Tutor 紀錄
    private Long newTutorUserId; // role=2, 尚未建立 Tutor 紀錄
    private Long studentUserId;  // role=1

    @BeforeEach
    void setUp() {
        User tutor = new User();
        tutor.setName("Existing Tutor Svc");
        tutor.setEmail("tutorsvc.existing@example.com");
        tutor.setPassword("hashed");
        tutor.setRole(2);
        tutor.setWallet(0L);
        tutor = userRepo.save(tutor);
        tutorUserId = tutor.getId();

        Tutor tutorProfile = new Tutor();
        tutorProfile.setUser(tutor);
        tutorProfile.setTitle("Math Teacher");
        tutorRepo.save(tutorProfile);

        User newTutor = new User();
        newTutor.setName("New Tutor Svc");
        newTutor.setEmail("tutorsvc.new@example.com");
        newTutor.setPassword("hashed");
        newTutor.setRole(2);
        newTutor.setWallet(0L);
        newTutor = userRepo.save(newTutor);
        newTutorUserId = newTutor.getId();

        User student = new User();
        student.setName("Student Svc");
        student.setEmail("tutorsvc.student@example.com");
        student.setPassword("hashed");
        student.setRole(1);
        student.setWallet(0L);
        student = userRepo.save(student);
        studentUserId = student.getId();
    }

    // ===================== getTutor =====================

    @Test
    void getTutor_existingId_returnsTutor() {
        Tutor result = tutorService.getTutor(tutorUserId);
        assertNotNull(result);
        assertEquals(tutorUserId, result.getId());
    }

    @Test
    void getTutor_nonExistingId_returnsNull() {
        Tutor result = tutorService.getTutor(999999L);
        assertNull(result);
    }

    // ===================== createTutor =====================

    @Test
    void createTutor_validTutorUser_returnsTrue() {
        TutorReq req = new TutorReq();
        req.setTutorId(newTutorUserId);
        req.setTitle("Science Teacher");

        assertTrue(tutorService.createTutor(req));
        assertTrue(tutorRepo.existsById(newTutorUserId));
    }

    @Test
    void createTutor_nullTutorId_returnsFalse() {
        TutorReq req = new TutorReq();
        req.setTutorId(null);

        assertFalse(tutorService.createTutor(req));
    }

    @Test
    void createTutor_nonExistentUser_returnsFalse() {
        TutorReq req = new TutorReq();
        req.setTutorId(999999L);

        assertFalse(tutorService.createTutor(req));
    }

    @Test
    void createTutor_nonTutorRole_returnsFalse() {
        TutorReq req = new TutorReq();
        req.setTutorId(studentUserId); // role=1

        assertFalse(tutorService.createTutor(req));
    }

    @Test
    void createTutor_duplicate_returnsFalse() {
        TutorReq req = new TutorReq();
        req.setTutorId(tutorUserId); // 已存在 Tutor 紀錄

        assertFalse(tutorService.createTutor(req));
    }

    // ===================== updateTutor =====================

    @Test
    void updateTutor_existingId_returnsTrue() {
        TutorReq req = new TutorReq();
        req.setTitle("Updated Title");

        assertTrue(tutorService.updateTutor(tutorUserId, req));

        Tutor updated = tutorRepo.findById(tutorUserId).orElseThrow();
        assertEquals("Updated Title", updated.getTitle());
    }

    @Test
    void updateTutor_nonExistingId_returnsFalse() {
        TutorReq req = new TutorReq();
        req.setTitle("Updated Title");

        assertFalse(tutorService.updateTutor(999999L, req));
    }

    // ===================== deleteTutor =====================

    @Test
    void deleteTutor_existingId_returnsTrue() {
        assertTrue(tutorService.deleteTutor(tutorUserId));
        assertFalse(tutorRepo.existsById(tutorUserId));
    }

    @Test
    void deleteTutor_nonExistingId_returnsFalse() {
        assertFalse(tutorService.deleteTutor(999999L));
    }
}
