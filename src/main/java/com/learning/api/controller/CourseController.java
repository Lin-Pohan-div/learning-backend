package com.learning.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learning.api.dto.CourseReq;
import com.learning.api.entity.Course;
import com.learning.api.service.CourseService;

@RestController
@RequestMapping("/api/tutor/{tutorId}/courses")
@CrossOrigin(origins = "http://localhost:5173")
public class CourseController {

    @Autowired
    private CourseService courseService;

    // GET /api/tutor/{tutorId}/courses
    @GetMapping
    public ResponseEntity<List<Course>> getCourses(@PathVariable Long tutorId) {
        return ResponseEntity.ok(courseService.getCoursesByTutorId(tutorId));
    }

    // GET /api/tutor/{tutorId}/courses/{courseId}
    @GetMapping("/{courseId}")
    public ResponseEntity<Course> getCourse(
            @PathVariable Long tutorId,
            @PathVariable Long courseId) {
        return ResponseEntity.ok(courseService.getCourse(tutorId, courseId));
    }

    // POST /api/tutor/{tutorId}/courses
    @PostMapping
    public ResponseEntity<Course> createCourse(
            @PathVariable Long tutorId,
            @RequestBody CourseReq dto) {
        return ResponseEntity.ok(courseService.createCourse(tutorId, dto));
    }

    // PUT /api/tutor/{tutorId}/courses/{courseId}
    @PutMapping("/{courseId}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long tutorId,
            @PathVariable Long courseId,
            @RequestBody CourseReq dto) {
        return ResponseEntity.ok(courseService.updateCourse(tutorId, courseId, dto));
    }

    // DELETE /api/tutor/{tutorId}/courses/{courseId}
    @DeleteMapping("/{courseId}")
    public ResponseEntity<String> deleteCourse(
            @PathVariable Long tutorId,
            @PathVariable Long courseId) {
        courseService.deleteCourse(tutorId, courseId);
        return ResponseEntity.ok("課程已刪除");
    }
}
