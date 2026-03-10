package com.learning.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.learning.api.dto.FeedbackRequest;
import com.learning.api.entity.LessonFeedback;
import com.learning.api.service.LessonFeedbackService;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/Feedbacks")
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
        return ResponseEntity.ok(Map.of(
                "bookingId", bookingId,
                "averageRating", avg != null ? avg : 0.0
        ));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody FeedbackRequest request) {
        try {
            if (request.getBookingId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("驗證失敗: bookingId 不能為空"));
            }
            if (request.getRating() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("驗證失敗: rating 不能為空"));
            }

            LessonFeedback feedback = new LessonFeedback();
            feedback.setBookingId(request.getBookingId());
            feedback.setRating(request.getRating());
            feedback.setComment(request.getComment());

            LessonFeedback saved = lessonFeedbackService.save(feedback);
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
    public ResponseEntity<LessonFeedback> update(@PathVariable Long id, @RequestBody LessonFeedback feedback) {
        return lessonFeedbackService.update(id, feedback)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return lessonFeedbackService.deleteById(id)
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
