package com.learning.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.learning.api.dto.ReviewRequest;
import com.learning.api.entity.Review;
import com.learning.api.service.ReviewService;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public List<Review> getAll() {
        return reviewService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getById(@PathVariable Long id) {
        return reviewService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public List<Review> getByUserId(@PathVariable Long userId) {
        return reviewService.findByUserId(userId);
    }

    @GetMapping("/course/{courseId}")
    public List<Review> getByCourseId(@PathVariable Long courseId) {
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
    public ResponseEntity<?> create(@RequestBody ReviewRequest request) {
        try {
            if (request.getUserId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("驗證失敗: userId 不能為空"));
            }
            if (request.getCourseId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("驗證失敗: courseId 不能為空"));
            }
            if (request.getRating() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("驗證失敗: rating 不能為空"));
            }

            Review review = new Review();
            review.setUserId(request.getUserId());
            review.setCourseId(request.getCourseId());
            review.setRating(request.getRating());
            review.setComment(request.getComment());

            Review saved = reviewService.save(review);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("錯誤: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("驗證失敗: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("伺服器錯誤: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Review> update(@PathVariable Long id, @RequestBody Review review) {
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("伺服器錯誤: " + e.getMessage()));
    }

    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
