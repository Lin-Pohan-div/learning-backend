package com.learning.api.controller;

import com.learning.api.annotation.ApiController;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.repo.TutorScheduleRepo;
import com.learning.api.service.TutorScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@ApiController
@RequestMapping("/api/teacher/schedules")
@RequiredArgsConstructor
public class TutorScheduleController {

    private final TutorScheduleService scheduleService;
    private final TutorScheduleRepo scheduleRepo;

    // [POST] 老師新增一個空閒時段
    @PostMapping
    public ResponseEntity<?> addSchedule(@RequestBody TutorSchedule schedule) {
        String result = scheduleService.addSchedule(schedule);
        if (!"success".equals(result)) {
            return ResponseEntity.status(400).body(Map.of("message", result));
        }
        return ResponseEntity.ok(Map.of("message", "排班成功！該時段已開放給家長預約。"));
    }

    // [GET] 取得某位老師所有的排班表
    @GetMapping("/{tutorId}")
    public ResponseEntity<?> getTutorSchedules(@PathVariable Long tutorId) {
        List<TutorSchedule> schedules = scheduleRepo.findByTutorId(tutorId);
        return ResponseEntity.ok(Map.of("tutorId", tutorId, "schedules", schedules));
    }
}
