package com.learning.api.controller;

import com.learning.api.entity.TutorSchedule;
import com.learning.api.repo.TutorScheduleRepo;
import com.learning.api.service.TutorScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/teacher/schedules")
public class TutorScheduleController {

    @Autowired
    private TutorScheduleService scheduleService;

    @Autowired
    private TutorScheduleRepo scheduleRepo;

    // [POST] 老師新增一個空閒時段
    @PostMapping
    public ResponseEntity<?> addSchedule(@RequestBody TutorSchedule schedule) {
        System.out.println("【大師監視器】收到排班請求：" + schedule);

        String result = scheduleService.addSchedule(schedule);

        if (!"success".equals(result)) {
            return ResponseEntity.status(400).body(Map.of("msg", result));
        }

        return ResponseEntity.ok(Map.of("msg", "排班成功！該時段已開放給家長預約。"));
    }

    // [GET] 取得某位老師所有的排班表 (未來給前端畫行事曆用)
    @GetMapping("/{tutorId}")
    public ResponseEntity<?> getTutorSchedules(@PathVariable Long tutorId) {
        List<TutorSchedule> schedules = scheduleRepo.findByTutorId(tutorId);
        return ResponseEntity.ok(Map.of(
                "tutorId", tutorId,
                "schedules", schedules
        ));
    }
}