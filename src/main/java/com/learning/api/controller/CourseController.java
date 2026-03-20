package com.learning.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learning.api.dto.CourseDTO;
import com.learning.api.dto.CourseReq;
import com.learning.api.security.SecurityUser;
import com.learning.api.service.CourseService;

@RestController
@RequestMapping("/api/tutor/me/courses")
@CrossOrigin(origins = "http://localhost:5173")
public class CourseController {

    @Autowired
    private CourseService courseService;

    // GET /api/tutor/me/courses
    @GetMapping
    public ResponseEntity<List<CourseDTO>> getCourses(
            @AuthenticationPrincipal SecurityUser me) {
        return ResponseEntity.ok(courseService.getCoursesByTutorId(me.getUser().getId()));
    }

    // GET /api/tutor/me/courses/{courseId}
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseDTO> getCourse(
            @AuthenticationPrincipal SecurityUser me,
            @PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.getCourse(me.getUser().getId(), courseId));
    }

    // POST /api/tutor/me/courses
    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(
            @AuthenticationPrincipal SecurityUser me,
            @RequestBody CourseReq dto) {
        return ResponseEntity.ok(courseService.createCourse(me.getUser().getId(), dto));
    }

    // PUT /api/tutor/me/courses/{courseId}
    @PutMapping("/{courseId}")
    public ResponseEntity<CourseDTO> updateCourse(
            @AuthenticationPrincipal SecurityUser me,
            @PathVariable Long courseId,
            @RequestBody CourseReq dto) {
        return ResponseEntity.ok(courseService.updateCourse(me.getUser().getId(), courseId, dto));
    }

    // DELETE /api/tutor/me/courses/{courseId}
    @DeleteMapping("/{courseId}")
    public ResponseEntity<String> deleteCourse(
            @AuthenticationPrincipal SecurityUser me,
            @PathVariable Long courseId) {
        courseService.deleteCourse(me.getUser().getId(), courseId);
        return ResponseEntity.ok("課程已刪除");
    }
}
