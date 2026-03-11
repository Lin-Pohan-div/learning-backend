package com.learning.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.learning.api.dto.FeedbackRequest;
import com.learning.api.entity.Feedback;
import com.learning.api.service.FeedbackService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService lessonFeedbackService;

    @GetMapping
    public List<Feedback> getAll() {
        return lessonFeedbackService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Feedback> getById(@PathVariable Long id) {
        return lessonFeedbackService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/lesson/{bookingId}")
    public List<Feedback> getByBookingId(@PathVariable Long bookingId) {
        return lessonFeedbackService.findByBookingId(bookingId);
    }

    @GetMapping("/lesson/{bookingId}/average-rating")
    public ResponseEntity<Map<String, Object>> getAverageRating(@PathVariable Long bookingId) {
        Double avg = lessonFeedbackService.getAverageRating(bookingId);
        return ResponseEntity.ok(Map.of("bookingId", bookingId, "averageRating", avg));
    }

    @PostMapping
    public ResponseEntity<Feedback> create(@RequestBody FeedbackRequest request) {
        Feedback feedback = toEntity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonFeedbackService.save(feedback));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Feedback> update(@PathVariable Long id, @RequestBody FeedbackRequest request) {
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleValidation(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "伺服器錯誤: " + e.getMessage()));
    }

    private Feedback toEntity(FeedbackRequest request) {
        Feedback feedback = new Feedback();
        feedback.setBookingId(request.getBookingId());
        feedback.setFocusScore(request.getFocusScore());
        feedback.setComprehensionScore(request.getComprehensionScore());
        feedback.setConfidenceScore(request.getConfidenceScore());
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());
        return feedback;
    }
}
