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

}
