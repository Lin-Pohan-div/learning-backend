package com.learning.api.controller;

import com.learning.api.dto.ScheduleDTO;
import com.learning.api.security.SecurityUser;
import com.learning.api.service.TutorScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*") // 允許前端跨域請求
@RestController
@RequestMapping("/api/tutor/schedules")
public class TutorScheduleController {
    @Autowired
    private TutorScheduleService scheduleService;

    @PostMapping("me/batch-toggle")
    public ResponseEntity<?> batchToggle(
            @RequestBody ScheduleDTO.BatchToggleReq req,
            @AuthenticationPrincipal SecurityUser me) {

        String result = scheduleService.batchToggle(req, me);
        if (!"success".equals(result)) {
            return ResponseEntity.badRequest().body(Map.of("msg", result));
        }
        return ResponseEntity.ok(Map.of("msg", "批次更新成功"));
    }

    // 2. 獲取老師「常態性的一週課表」
    // 呼叫範例: GET /api/teacher/schedules/2
    @GetMapping("/{tutorId}")
    public ResponseEntity<?> getSchedule(@PathVariable Long tutorId) {
        List<ScheduleDTO.Res> schedules = scheduleService.getWeeklySchedule(tutorId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("me")
    public ResponseEntity<?> getSchedule(@AuthenticationPrincipal SecurityUser me) {
        Long tutorId = me.getUser().getId();
        List<ScheduleDTO.Res> schedules = scheduleService.getWeeklySchedule(tutorId);
        return ResponseEntity.ok(schedules);
    }
}
