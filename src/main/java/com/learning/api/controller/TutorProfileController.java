package com.learning.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learning.api.dto.TutorUpdateDTO;
import com.learning.api.service.TutorService;

@RestController
@RequestMapping("/api/tutor/{tutorId}/profile")
@CrossOrigin(origins = "http://localhost:5173")
public class TutorProfileController {

    @Autowired
    private TutorService tutorService;

    // GET /api/tutor/{tutorId}/profile
    @GetMapping
    public ResponseEntity<TutorUpdateDTO> getProfile(@PathVariable Long tutorId) {
        return ResponseEntity.ok(tutorService.getProfileDTO(tutorId));
    }

    // PUT /api/tutor/{tutorId}/profile
    @PutMapping
    public ResponseEntity<String> updateProfile(
            @PathVariable Long tutorId,
            @RequestBody TutorUpdateDTO dto) {
        tutorService.updateProfile(tutorId, dto);
        return ResponseEntity.ok("個人資料已更新");
    }
}
