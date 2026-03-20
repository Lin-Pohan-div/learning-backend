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
import com.learning.api.service.TutorService;

@RestController
@RequestMapping("/api/tutor/me/profile")
@CrossOrigin(origins = "http://localhost:5173")
public class TutorProfileController {

    @Autowired
    private TutorService tutorService;

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
        tutorService.updateProfile(me.getUser().getId(), dto);
        return ResponseEntity.ok("個人資料已更新");
    }
}
