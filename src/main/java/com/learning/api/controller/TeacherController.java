package com.learning.api.controller;

import com.learning.api.annotation.ApiController;
import com.learning.api.dto.course.CourseReq;
import com.learning.api.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@ApiController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final CourseService courseService;

    // [POST] 新增課程 API
    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@RequestBody CourseReq courseReq) {
        if (!courseService.sendCourses(courseReq)) {
            return ResponseEntity.status(400).body(Map.of("msg", "新增課程失敗，請檢查資料格式或價格"));
        }
        return ResponseEntity.ok(Map.of("msg", "課程新增成功！學生現在可以購買了！"));
    }
}
