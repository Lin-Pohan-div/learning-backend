package com.learning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.api.entity.User;
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
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class TeacherControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepo;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Long tutorUserId;

    @BeforeEach
    void setUp() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        User tutor = new User();
        tutor.setName("Teacher Ctrl Tutor");
        tutor.setEmail("teacherctrl.tutor@example.com");
        tutor.setPassword("hashed");
        tutor.setRole(UserRole.TUTOR);
        tutor.setWallet(0L);
        tutor = userRepo.save(tutor);
        tutorUserId = tutor.getId();
    }

    // ===================== POST /api/teacher/courses =====================

    @Test
    void createCourse_validRequest_shouldReturn200() throws Exception {
        Map<String, Object> body = Map.of(
                "tutorId", tutorUserId,
                "subject", 11,
                "price", 500,
                "name", "Test Course",
                "active", true
        );

        mockMvc.perform(post("/api/teacher/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("課程新增成功！學生現在可以購買了！"));
    }

    @Test
    void createCourse_nullTutorId_shouldReturn400() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("tutorId", null);
        body.put("subject", 11);
        body.put("price", 500);
        body.put("name", "No Tutor Course");
        body.put("active", true);

        mockMvc.perform(post("/api/teacher/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("新增課程失敗，請檢查資料格式或價格"));
    }

    @Test
    void createCourse_zeroPrice_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "tutorId", tutorUserId,
                "subject", 11,
                "price", 0,
                "name", "Zero Price Course",
                "active", true
        );

        mockMvc.perform(post("/api/teacher/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("新增課程失敗，請檢查資料格式或價格"));
    }

    @Test
    void createCourse_descriptionTooLong_shouldReturn400() throws Exception {
        String longDesc = "A".repeat(1001);

        Map<String, Object> body = Map.of(
                "tutorId", tutorUserId,
                "subject", 11,
                "price", 500,
                "name", "Long Desc Course",
                "description", longDesc,
                "active", true
        );

        mockMvc.perform(post("/api/teacher/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("新增課程失敗，請檢查資料格式或價格"));
    }
}
