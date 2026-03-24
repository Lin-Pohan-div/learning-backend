package com.learning.api.controller;

import com.learning.api.security.SecurityUser;
import com.learning.api.service.Chat.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/tutor/me/upload")
@CrossOrigin(origins = "http://localhost:5173")
public class TutorUploadController {

    //老師上傳檔案至個人資料

    @Autowired
    private FileStorageService fileStorageService;

    // POST /api/tutor/me/upload/avatar
    @PostMapping("/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @AuthenticationPrincipal SecurityUser me,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("url", fileStorageService.saveImage(file)));
    }

    // POST /api/tutor/me/upload/certificate1
    @PostMapping("/certificate1")
    public ResponseEntity<Map<String, String>> uploadCertificate1(
            @AuthenticationPrincipal SecurityUser me,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("url", fileStorageService.saveImage(file)));
    }
 
    // POST /api/tutor/me/upload/certificate2
    @PostMapping("/certificate2")
    public ResponseEntity<Map<String, String>> uploadCertificate2(
            @AuthenticationPrincipal SecurityUser me,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("url", fileStorageService.saveImage(file)));
    }

    // POST /api/tutor/me/upload/video1
    @PostMapping("/video1")
    public ResponseEntity<Map<String, String>> uploadVideo1(
            @AuthenticationPrincipal SecurityUser me,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("url", fileStorageService.saveVideo(file)));
    }

    // POST /api/tutor/me/upload/video2
    @PostMapping("/video2")
    public ResponseEntity<Map<String, String>> uploadVideo2(
            @AuthenticationPrincipal SecurityUser me,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(Map.of("url", fileStorageService.saveVideo(file)));
    }
}
