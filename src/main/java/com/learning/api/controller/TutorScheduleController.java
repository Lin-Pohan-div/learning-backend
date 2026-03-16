package com.learning.api.controller;

import com.learning.api.dto.ScheduleDTO;
import com.learning.api.service.TutorScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*") // 允許前端跨域請求
@RestController
@RequestMapping("/api/teacher/schedules")
public class TutorScheduleController {

    @Autowired
    private TutorScheduleService scheduleService;

    // 1. 老師點擊格子切換狀態 (開放/關閉)
    // 呼叫範例: POST /api/teacher/schedules/toggle
    @PostMapping("/toggle")
    public ResponseEntity<?> toggleSlot(@RequestBody ScheduleDTO.ToggleReq req) {
        String result = scheduleService.toggleSchedule(req);

        if (!"success".equals(result)) {
            // 如果失敗 (例如時間格式錯誤)，回傳 400 錯誤與訊息
            return ResponseEntity.badRequest().body(Map.of("msg", result));
        }

        return ResponseEntity.ok(Map.of("msg", "時段狀態已更新"));
    }

    // 2. 獲取老師「常態性的一週課表」
    // 呼叫範例: GET /api/teacher/schedules/2
    @GetMapping("/{tutorId}")
    public ResponseEntity<?> getSchedule(@PathVariable Long tutorId) {
        List<ScheduleDTO.Res> schedules = scheduleService.getWeeklySchedule(tutorId);
        return ResponseEntity.ok(schedules);
    }
}