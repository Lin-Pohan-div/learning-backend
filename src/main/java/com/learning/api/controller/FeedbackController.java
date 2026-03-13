package com.learning.api.controller;

import com.learning.api.annotation.ApiController;
import com.learning.api.dto.FeedbackRequest;
import com.learning.api.entity.LessonFeedback;
import com.learning.api.service.LessonFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@ApiController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final LessonFeedbackService lessonFeedbackService;

    @GetMapping
    public List<LessonFeedback> getAll() {
        return lessonFeedbackService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonFeedback> getById(@PathVariable Long id) {
        return lessonFeedbackService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/lesson/{bookingId}")
    public List<LessonFeedback> getByBookingId(@PathVariable Long bookingId) {
        return lessonFeedbackService.findByBookingId(bookingId);
    }

    @GetMapping("/lesson/{bookingId}/average-rating")
    public ResponseEntity<Map<String, Object>> getAverageRating(@PathVariable Long bookingId) {
        Double avg = lessonFeedbackService.getAverageRating(bookingId);
        return ResponseEntity.ok(Map.of("bookingId", bookingId, "averageRating", avg));
    }

    @PostMapping
    public ResponseEntity<LessonFeedback> create(@RequestBody FeedbackRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonFeedbackService.save(toEntity(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LessonFeedback> update(@PathVariable Long id, @RequestBody FeedbackRequest request) {
        return lessonFeedbackService.update(id, toEntity(request))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return lessonFeedbackService.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private LessonFeedback toEntity(FeedbackRequest request) {
        LessonFeedback feedback = new LessonFeedback();
        feedback.setBookingId(request.getBookingId());
        feedback.setFocusScore(request.getFocusScore());
        feedback.setComprehensionScore(request.getComprehensionScore());
        feedback.setConfidenceScore(request.getConfidenceScore());
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());
        return feedback;
    }
}
