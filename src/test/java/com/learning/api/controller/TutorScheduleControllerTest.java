package com.learning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.entity.User;
import com.learning.api.repo.TutorScheduleRepo;
import com.learning.api.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.learning.api.enums.UserRole;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class TutorScheduleControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private TutorScheduleRepo scheduleRepo;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Long tutorId;

    @BeforeEach
    void setUp() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        User tutor = new User();
        tutor.setName("Schedule Tutor");
        tutor.setEmail("schedule.tutor@example.com");
        tutor.setPassword("hashed");
        tutor.setRole(UserRole.TUTOR);
        tutor.setWallet(0L);
        tutor = userRepo.save(tutor);
        tutorId = tutor.getId();
    }

    // ===================== POST /api/teacher/schedules/toggle =====================

    @Test
    void toggleSlot_setAvailable_noExisting_shouldReturn200() throws Exception {
        Map<String, Object> body = Map.of(
                "tutorId", tutorId,
                "weekday", 1,
                "hour", 10,
                "targetStatus", "available"
        );

        mockMvc.perform(post("/api/teacher/schedules/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("時段狀態已更新"));
    }

    @Test
    void toggleSlot_setInactive_existingRecord_shouldReturn200() throws Exception {
        // 先建立一筆課表
        TutorSchedule schedule = new TutorSchedule();
        schedule.setTutorId(tutorId);
        schedule.setWeekday(2);
        schedule.setHour(14);
        schedule.setStatus("available");
        scheduleRepo.save(schedule);

        Map<String, Object> body = Map.of(
                "tutorId", tutorId,
                "weekday", 2,
                "hour", 14,
                "targetStatus", "inactive"
        );

        mockMvc.perform(post("/api/teacher/schedules/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("時段狀態已更新"));
    }

    @Test
    void toggleSlot_invalidHour_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "tutorId", tutorId,
                "weekday", 1,
                "hour", 8,   // 8 < 9，超出範圍
                "targetStatus", "available"
        );

        mockMvc.perform(post("/api/teacher/schedules/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").isNotEmpty());
    }

    @Test
    void toggleSlot_invalidWeekday_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "tutorId", tutorId,
                "weekday", 0,  // 0 < 1，超出範圍
                "hour", 10,
                "targetStatus", "available"
        );

        mockMvc.perform(post("/api/teacher/schedules/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").isNotEmpty());
    }

    // ===================== GET /api/teacher/schedules/{tutorId} =====================

    @Test
    void getSchedule_withRecords_shouldReturnList() throws Exception {
        TutorSchedule schedule = new TutorSchedule();
        schedule.setTutorId(tutorId);
        schedule.setWeekday(3);
        schedule.setHour(15);
        schedule.setStatus("available");
        scheduleRepo.save(schedule);

        mockMvc.perform(get("/api/teacher/schedules/{tutorId}", tutorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void getSchedule_noRecords_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/teacher/schedules/{tutorId}", tutorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
