package com.learning.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.learning.api.dto.LessonFeedbackRequest;
import com.learning.api.entity.LessonFeedback;
import com.learning.api.service.LessonFeedbackService;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/lesson-feedbacks")
@RequiredArgsConstructor
public class LessonFeedbackController {

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

    @GetMapping("/lesson/{lessonId}")
    public List<LessonFeedback> getByLessonId(@PathVariable Long lessonId) {
        return lessonFeedbackService.findByLessonId(lessonId);
    }

    @GetMapping("/lesson/{lessonId}/average-rating")
    public ResponseEntity<Map<String, Object>> getAverageRating(@PathVariable Long lessonId) {
        Double avg = lessonFeedbackService.getAverageRating(lessonId);
        return ResponseEntity.ok(Map.of(
                "lessonId", lessonId,
                "averageRating", avg != null ? avg : 0.0
        ));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody LessonFeedbackRequest request) {
        try {
            if (request.getLessonId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("驗證失敗: lessonId 不能為空"));
            }
            if (request.getRating() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("驗證失敗: rating 不能為空"));
            }

            LessonFeedback feedback = new LessonFeedback();
            feedback.setLessonId(request.getLessonId());
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
