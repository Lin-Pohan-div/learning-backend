package com.learning.api.controller;

import com.learning.api.dto.TutorProfileDTO;
import com.learning.api.service.TutorProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/teacher/profile")
public class TutorProfileController {

    @Autowired
    private TutorProfileService profileService;

    // [PUT] 更新老師個人檔案 (業界常態：更新資料用 PUT 請求)
    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody TutorProfileDTO dto) {

        System.out.println("【大師監視器】收到更新檔案請求：" + dto);

        if (dto.getTutorId() == null) {
            return ResponseEntity.status(400).body(Map.of("msg", "必須提供老師 ID"));
        }

        boolean success = profileService.updateProfile(dto);

        if (!success) {
            return ResponseEntity.status(404).body(Map.of("msg", "更新失敗，找不到該名老師"));
        }

        return ResponseEntity.ok(Map.of("msg", "個人檔案儲存成功！您的學生現在可以看到最新資訊了！"));
    }
}