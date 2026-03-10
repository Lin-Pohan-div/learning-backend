package com.learning.api.controller;

import com.learning.api.entity.Course;
import com.learning.api.service.TeacherCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/teacher") // 網址開頭都會是 /api/teacher/...
public class TeacherController {

    @Autowired
    private TeacherCourseService teacherCourseService;

    // [POST] 新增課程 API
    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@RequestBody Course course) {

        System.out.println("【大師監視器】收到老師開課請求：" + course);

        boolean isSuccess = teacherCourseService.addCourse(course);

        if (!isSuccess) {
            return ResponseEntity.status(400).body(Map.of("msg", "新增課程失敗，請檢查資料格式或價格"));
        }

        return ResponseEntity.ok(Map.of("msg", "課程新增成功！學生現在可以購買了！"));
    }
}