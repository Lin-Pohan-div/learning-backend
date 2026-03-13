package com.learning.api.controller;

import com.learning.api.annotation.ApiController;
import com.learning.api.dto.TutorProfileDTO;
import com.learning.api.entity.Tutor;
import com.learning.api.service.TutorProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@ApiController
@RequestMapping("/api/teacher/profile")
@RequiredArgsConstructor
public class TutorProfileController {

    private final TutorProfileService profileService;

    // [GET] 取得老師個人檔案
    @GetMapping("/{tutorId}")
    public ResponseEntity<?> getProfile(@PathVariable Long tutorId) {
        Tutor tutor = profileService.getProfile(tutorId);
        if (tutor == null) {
            return ResponseEntity.status(404).body(Map.of("msg", "找不到該名老師的個人檔案"));
        }
        return ResponseEntity.ok(tutor);
    }

    // [POST] 建立老師個人檔案（初次設定）
    @PostMapping
    public ResponseEntity<?> createProfile(@RequestBody TutorProfileDTO dto) {
        if (dto.getTutorId() == null) {
            return ResponseEntity.status(400).body(Map.of("message", "必須提供老師 ID"));
        }

        String result = profileService.createProfile(dto);

        if (result.equals("success")) {
            return ResponseEntity.status(201).body(Map.of("msg", "個人檔案建立成功！"));
        } else if (result.contains("已存在")) {
            return ResponseEntity.status(409).body(Map.of("msg", result));
        } else {
            return ResponseEntity.status(404).body(Map.of("msg", result));
        }
    }

    // [PUT] 更新老師個人檔案
    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody TutorProfileDTO dto) {
        if (dto.getTutorId() == null) {
            return ResponseEntity.status(400).body(Map.of("msg", "必須提供老師 ID"));
        }

        String result = profileService.updateProfile(dto);

        if (!result.equals("success")) {
            return ResponseEntity.status(404).body(Map.of("msg", result));
        }

        return ResponseEntity.ok(Map.of("msg", "個人檔案更新成功！"));
    }

    // [DELETE] 刪除老師個人檔案
    @DeleteMapping("/{tutorId}")
    public ResponseEntity<?> deleteProfile(@PathVariable Long tutorId) {
        String result = profileService.deleteProfile(tutorId);

        if (!result.equals("success")) {
            return ResponseEntity.status(404).body(Map.of("msg", result));
        }

        return ResponseEntity.ok(Map.of("msg", "個人檔案已成功刪除！"));
    }
}
