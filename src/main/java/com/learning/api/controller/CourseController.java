package com.learning.api.controller;

import com.learning.api.annotation.ApiController;
import com.learning.api.dto.CourseReq;
import com.learning.api.dto.CourseResp;
import com.learning.api.entity.Course;
import com.learning.api.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@ApiController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<?> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable Long id) {
        CourseResp resp = courseService.getCourseById(id);
        if (resp == null) return ResponseEntity.status(404).body(Map.of("msg", "課程不存在"));
        return ResponseEntity.ok(resp);
    }

    // GET 老師所有課程（不分上下架）
    @GetMapping("/tutor/{tutorId}")
    public List<Course> getByTutorId(@PathVariable Long tutorId) {
        return courseService.findByTutorId(tutorId);
    }

    // GET 老師已上架課程
    @GetMapping("/tutor/{tutorId}/active")
    public List<Course> getByTutorIdActive(@PathVariable Long tutorId) {
        return courseService.findByTutorIdActive(tutorId);
    }

    // POST 建立課程
    @PostMapping
    public ResponseEntity<?> sendCourses(@RequestBody CourseReq courseReq) {
        if (!courseService.sendCourses(courseReq)) return ResponseEntity.status(400).body(Map.of("msg", "建立失敗"));
        return ResponseEntity.ok(Map.of("msg", "ok"));
    }

    // PUT 更新課程
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable Long id, @RequestBody CourseReq courseReq) {
        try {
            return courseService.updateCourse(id, courseReq)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("驗證失敗: " + e.getMessage()));
        }
    }

    // DELETE 刪除課程
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        return courseService.deleteById(id)
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
