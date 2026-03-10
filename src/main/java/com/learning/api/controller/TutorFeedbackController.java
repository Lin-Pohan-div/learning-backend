package com.learning.api.controller;

import com.learning.api.entity.LessonFeedback;
import com.learning.api.repo.LessonFeedbackRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/teacher/feedbacks")
public class TutorFeedbackController {

    @Autowired
    private LessonFeedbackRepo feedbackRepo;

    // [POST] 老師送出課後回饋
    @PostMapping
    public ResponseEntity<?> submitFeedback(@RequestBody LessonFeedback feedback) {

        System.out.println("【大師監視器】收到課後回饋：" + feedback.getComment());

        if (feedback.getRating() < 1 || feedback.getRating() > 5) {
            return ResponseEntity.status(400).body(Map.of("msg", "評分必須介於 1 到 5 之間"));
        }

        // 2. 檢查是否已經填寫過
        if (feedbackRepo.existsByBookingId(feedback.getBookingId())) {
            return ResponseEntity.status(400).body(Map.of("msg", "這堂課已經填寫過回饋囉！"));
        }

        // 3. 存入資料庫
        feedbackRepo.save(feedback);

        return ResponseEntity.ok(Map.of("msg", "課後回饋送出成功！家長將會收到通知。"));
    }
}