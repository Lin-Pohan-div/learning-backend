package com.learning.api.controller;

import com.learning.api.annotation.ApiController;
import com.learning.api.dto.ReviewRequest;
import com.learning.api.entity.Reviews;
import com.learning.api.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@ApiController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public List<Reviews> getAll() {
        return reviewService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reviews> getById(@PathVariable Long id) {
        return reviewService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public List<Reviews> getByUserId(@PathVariable Long userId) {
        return reviewService.findByUserId(userId);
    }

    @GetMapping("/course/{courseId}")
    public List<Reviews> getByCourseId(@PathVariable Long courseId) {
        return reviewService.findByCourseId(courseId);
    }

    @GetMapping("/course/{courseId}/average-rating")
    public ResponseEntity<Map<String, Object>> getAverageRating(@PathVariable Long courseId) {
        Double avg = reviewService.getAverageRating(courseId);
        return ResponseEntity.ok(Map.of(
                "courseId", courseId,
                "averageRating", avg != null ? avg : 0.0
        ));
    }

    @PostMapping
    public ResponseEntity<Reviews> create(@RequestBody ReviewRequest request) {
        if (request.getUserId() == null) throw new IllegalArgumentException("驗證失敗: userId 不能為空");
        if (request.getCourseId() == null) throw new IllegalArgumentException("驗證失敗: courseId 不能為空");

        Reviews review = new Reviews();
        review.setUserId(request.getUserId());
        review.setCourseId(request.getCourseId());
        review.setFocusScore(request.getFocusScore());
        review.setComprehensionScore(request.getComprehensionScore());
        review.setConfidenceScore(request.getConfidenceScore());
        review.setComment(request.getComment());

        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.save(review));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reviews> update(@PathVariable Long id, @RequestBody Reviews review) {
        return reviewService.update(id, review)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return reviewService.deleteById(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
