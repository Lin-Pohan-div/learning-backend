package com.learning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.User;
import com.learning.api.repo.TutorRepository;
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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class TutorControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private TutorRepository tutorRepo;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private Long tutorUserId;    // role=2, 已建立 Tutor 紀錄
    private Long newTutorUserId; // role=2, 尚未建立 Tutor 紀錄
    private Long studentUserId;  // role=1

    @BeforeEach
    void setUp() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 已存在的老師 (有 Tutor 紀錄)
        User tutor = new User();
        tutor.setName("Existing Tutor");
        tutor.setEmail("existing.tutor@example.com");
        tutor.setPassword("hashed");
        tutor.setRole(2);
        tutor.setWallet(0L);
        tutor = userRepo.save(tutor);
        tutorUserId = tutor.getId();

        Tutor tutorProfile = new Tutor();
        tutorProfile.setUser(tutor);
        tutorProfile.setTitle("Math Teacher");
        tutorRepo.save(tutorProfile);

        // 新老師 (role=2，尚未建立 Tutor 紀錄)
        User newTutor = new User();
        newTutor.setName("New Tutor");
        newTutor.setEmail("new.tutor@example.com");
        newTutor.setPassword("hashed");
        newTutor.setRole(2);
        newTutor.setWallet(0L);
        newTutor = userRepo.save(newTutor);
        newTutorUserId = newTutor.getId();

        // 學生 (role=1)
        User student = new User();
        student.setName("Test Student");
        student.setEmail("tutor.ctrl.student@example.com");
        student.setPassword("hashed");
        student.setRole(1);
        student.setWallet(0L);
        student = userRepo.save(student);
        studentUserId = student.getId();
    }

    // ===================== GET /api/tutor/{id} =====================

    @Test
    void getTutor_existingId_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/tutor/{id}", tutorUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tutorUserId));
    }

    @Test
    void getTutor_nonExistingId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/tutor/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.msg").value("查無老師資料"));
    }

    // ===================== POST /api/tutor =====================

    @Test
    void createTutor_validRequest_shouldReturn200() throws Exception {
        Map<String, Object> body = Map.of("tutorId", newTutorUserId);

        mockMvc.perform(post("/api/tutor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("ok"));
    }

    @Test
    void createTutor_nonExistentUser_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of("tutorId", 999999L);

        mockMvc.perform(post("/api/tutor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("建立失敗"));
    }

    @Test
    void createTutor_nonTutorRole_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of("tutorId", studentUserId);

        mockMvc.perform(post("/api/tutor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("建立失敗"));
    }

    @Test
    void createTutor_duplicate_shouldReturn400() throws Exception {
        // tutorUserId 已有 Tutor 紀錄
        Map<String, Object> body = Map.of("tutorId", tutorUserId);

        mockMvc.perform(post("/api/tutor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("建立失敗"));
    }

    // ===================== PUT /api/tutor/{id} =====================

    @Test
    void updateTutor_existingId_shouldReturn200() throws Exception {
        Map<String, Object> body = Map.of("title", "Updated Title");

        mockMvc.perform(put("/api/tutor/{id}", tutorUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("ok"));
    }

    @Test
    void updateTutor_nonExistingId_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of("title", "Updated Title");

        mockMvc.perform(put("/api/tutor/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("更新失敗"));
    }

    // ===================== DELETE /api/tutor/{id} =====================

    @Test
    void deleteTutor_existingId_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/tutor/{id}", tutorUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("ok"));
    }

    @Test
    void deleteTutor_nonExistingId_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/tutor/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.msg").value("查無老師資料"));
    }
}
