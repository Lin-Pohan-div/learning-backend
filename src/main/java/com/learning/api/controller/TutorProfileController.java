package com.learning.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learning.api.dto.TutorUpdateDTO;
import com.learning.api.security.SecurityUser;
import com.learning.api.service.Chat.FileStorageService;
import com.learning.api.service.TutorService;

@RestController
@RequestMapping("/api/tutor/me/profile")
@CrossOrigin(origins = "http://localhost:5173")
public class TutorProfileController {

    @Autowired
    private TutorService tutorService;

    @Autowired
    private FileStorageService fileStorageService;

    // GET /api/tutor/me/profile
    @GetMapping
    public ResponseEntity<TutorUpdateDTO> getProfile(
            @AuthenticationPrincipal SecurityUser me) {
        return ResponseEntity.ok(tutorService.getProfileDTO(me.getUser().getId()));
    }

    // PUT /api/tutor/me/profile
    @PutMapping
    public ResponseEntity<String> updateProfile(
            @AuthenticationPrincipal SecurityUser me,
            @RequestBody TutorUpdateDTO dto) {

        Long tutorId = me.getUser().getId();
        TutorUpdateDTO current = tutorService.getProfileDTO(tutorId);

        // 若有新的 URL 且與舊的不同，刪除舊的實體檔案
        deleteOldFileIfReplaced(current.getAvatar(),       dto.getAvatar());
        deleteOldFileIfReplaced(current.getCertificate1(), dto.getCertificate1());
        deleteOldFileIfReplaced(current.getCertificate2(), dto.getCertificate2());
        deleteOldFileIfReplaced(current.getVideoUrl1(),    dto.getVideoUrl1());
        deleteOldFileIfReplaced(current.getVideoUrl2(),    dto.getVideoUrl2());

        tutorService.updateProfile(tutorId, dto);
        return ResponseEntity.ok("個人資料已更新");
    }

    // ── 私有輔助方法 ──────────────────────────────────────────────────

    private void deleteOldFileIfReplaced(String oldUrl, String newUrl) {
        if (oldUrl != null && !oldUrl.equals(newUrl)) {
            fileStorageService.deleteFileByUrl(oldUrl);
        }
    }
}
