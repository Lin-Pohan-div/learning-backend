package com.learning.api.controller;

import com.learning.api.annotation.ApiController;
import com.learning.api.entity.Course;
import com.learning.api.service.TeacherCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@ApiController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherCourseService teacherCourseService;

    // [POST] 新增課程 API
    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@RequestBody Course course) {
        if (!teacherCourseService.addCourse(course)) {
            return ResponseEntity.status(400).body(Map.of("message", "新增課程失敗，請檢查資料格式或價格"));
        }
        return ResponseEntity.ok(Map.of("message", "課程新增成功！學生現在可以購買了！"));
    }
}
