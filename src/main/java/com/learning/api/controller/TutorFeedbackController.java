package com.learning.api.controller;

import com.learning.api.annotation.ApiController;
import com.learning.api.entity.LessonFeedback;
import com.learning.api.repo.LessonFeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@ApiController
@RequestMapping("/api/teacher/feedbacks")
@RequiredArgsConstructor
public class TutorFeedbackController {

    private final LessonFeedbackRepository feedbackRepo;

    // [POST] 老師送出課後回饋
    @PostMapping
    public ResponseEntity<?> submitFeedback(@RequestBody LessonFeedback feedback) {
        if (feedback.getRating() < 1 || feedback.getRating() > 5) {
            return ResponseEntity.status(400).body(Map.of("message", "評分必須介於 1 到 5 之間"));
        }
        if (feedbackRepo.existsByBookingId(feedback.getBookingId())) {
            return ResponseEntity.status(400).body(Map.of("message", "這堂課已經填寫過回饋囉！"));
        }
        feedbackRepo.save(feedback);
        return ResponseEntity.ok(Map.of("message", "課後回饋送出成功！家長將會收到通知。"));
    }
}
