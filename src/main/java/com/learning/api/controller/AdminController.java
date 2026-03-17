package com.learning.api.controller;

import com.learning.api.annotation.ApiController;
import com.learning.api.repo.TutorRepository;
import com.learning.api.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理後台 API — 需要 ADMIN role（SecurityConfig 已設定 /api/admin/** → ADMIN）
 */
@ApiController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepo;
    private final TutorRepository tutorRepo;

    /** 查詢所有用戶 */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepo.findAll());
    }

    /** 查詢待審核教師（status=1） */
    @GetMapping("/tutors/pending")
    public ResponseEntity<?> getPendingTutors() {
        var pending = tutorRepo.findAll().stream()
                .filter(t -> Integer.valueOf(1).equals(t.getStatus()))
                .toList();
        return ResponseEntity.ok(pending);
    }

    /** 核准教師（status 設為 2=qualified） */
    @PatchMapping("/tutors/{id}/approve")
    public ResponseEntity<?> approveTutor(@PathVariable Long id) {
        return tutorRepo.findById(id).map(tutor -> {
            tutor.setStatus(2);
            tutorRepo.save(tutor);
            return ResponseEntity.ok(Map.of("msg", "教師已核准"));
        }).orElse(ResponseEntity.status(404).body(Map.of("msg", "教師不存在")));
    }

    /** 停權教師（status 設為 3=suspended） */
    @PatchMapping("/tutors/{id}/suspend")
    public ResponseEntity<?> suspendTutor(@PathVariable Long id) {
        return tutorRepo.findById(id).map(tutor -> {
            tutor.setStatus(3);
            tutorRepo.save(tutor);
            return ResponseEntity.ok(Map.of("msg", "教師已停權"));
        }).orElse(ResponseEntity.status(404).body(Map.of("msg", "教師不存在")));
    }
}
