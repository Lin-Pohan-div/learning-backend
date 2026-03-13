package com.learning.api.controller;

import com.learning.api.dto.*;
import com.learning.api.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PostMapping
    public ResponseEntity<?> sendCourses(@RequestBody CourseReq courseReq){
        if (!courseService.sendCourses(courseReq)) return ResponseEntity.status(400).body(Map.of("msg", "建立失敗"));
        return ResponseEntity.ok(Map.of("msg", "ok"));
    }

    @GetMapping
    public ResponseEntity<?> getAllCourses(){
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<?> getCoursesByTutor(@PathVariable Long tutorId){
        return ResponseEntity.ok(courseService.getCoursesByTutor(tutorId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable Long id, @RequestBody CourseReq courseReq){
        if (!courseService.updateCourse(id, courseReq)) return ResponseEntity.status(400).body(Map.of("msg", "修改失敗"));
        return ResponseEntity.ok(Map.of("msg", "ok"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable Long id){
        if (!courseService.deleteCourse(id)) return ResponseEntity.status(404).body(Map.of("msg", "查無課程"));
        return ResponseEntity.ok(Map.of("msg", "ok"));
    }
}
