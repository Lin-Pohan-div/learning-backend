package com.learning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.api.dto.ScheduleDTO;
import com.learning.api.service.TutorScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TutorScheduleControllerTest {

    @Mock private TutorScheduleService scheduleService;

    @InjectMocks
    private TutorScheduleController scheduleController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(scheduleController).build();
    }

    // ── 測試資料工廠 ──────────────────────────────────────────────────────────

    private ScheduleDTO.ToggleReq makeToggleReq(Long tutorId, int weekday, int hour, String targetStatus) {
        ScheduleDTO.ToggleReq req = new ScheduleDTO.ToggleReq();
        req.setTutorId(tutorId);
        req.setWeekday(weekday);
        req.setHour(hour);
        req.setTargetStatus(targetStatus);
        return req;
    }

    // ── POST /api/teacher/schedules/toggle ────────────────────────────────────

    @Test
    void toggle_validAvailableRequest_returns200() throws Exception {
        when(scheduleService.toggleSchedule(any(ScheduleDTO.ToggleReq.class))).thenReturn("success");

        mockMvc.perform(post("/api/teacher/schedules/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeToggleReq(1L, 1, 10, "available"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("時段狀態已更新"));
    }

    @Test
    void toggle_validInactiveRequest_returns200() throws Exception {
        when(scheduleService.toggleSchedule(any(ScheduleDTO.ToggleReq.class))).thenReturn("success");

        mockMvc.perform(post("/api/teacher/schedules/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeToggleReq(1L, 3, 14, "inactive"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("時段狀態已更新"));
    }

    @Test
    void toggle_invalidHour_returns400WithMsg() throws Exception {
        when(scheduleService.toggleSchedule(any(ScheduleDTO.ToggleReq.class)))
                .thenReturn("格式錯誤：時間範圍需在 9~21 點之間。");

        mockMvc.perform(post("/api/teacher/schedules/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeToggleReq(1L, 1, 8, "available"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    void toggle_invalidWeekday_returns400WithMsg() throws Exception {
        when(scheduleService.toggleSchedule(any(ScheduleDTO.ToggleReq.class)))
                .thenReturn("格式錯誤：時間範圍需在 9~21 點之間。");

        mockMvc.perform(post("/api/teacher/schedules/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeToggleReq(1L, 0, 10, "available"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").exists());
    }

    // ── GET /api/teacher/schedules/{tutorId} ──────────────────────────────────

    @Test
    void getSchedule_withSchedules_returns200WithList() throws Exception {
        List<ScheduleDTO.Res> schedules = List.of(
                new ScheduleDTO.Res(1L, 1, 10, "available"),
                new ScheduleDTO.Res(2L, 3, 14, "available")
        );
        when(scheduleService.getWeeklySchedule(1L)).thenReturn(schedules);

        mockMvc.perform(get("/api/teacher/schedules/{tutorId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].weekday").value(1))
                .andExpect(jsonPath("$[0].hour").value(10))
                .andExpect(jsonPath("$[0].status").value("available"));
    }

    @Test
    void getSchedule_noSchedules_returns200WithEmptyList() throws Exception {
        when(scheduleService.getWeeklySchedule(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/teacher/schedules/{tutorId}", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
