package com.learning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.api.entity.Course;
import com.learning.api.repo.CourseRepo;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class CourseControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Long savedTutorId;
    private Long savedStudentId;
    private Long savedCourseId;

    @BeforeEach
    void setUp() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 建立老師 (role=2)
        com.learning.api.entity.User tutor = new com.learning.api.entity.User();
        tutor.setName("Test Tutor");
        tutor.setEmail("coursetest.tutor@example.com");
        tutor.setPassword("hashedpassword");
        tutor.setRole(UserRole.TUTOR);
        tutor.setWallet(0L);
        tutor = userRepository.save(tutor);
        savedTutorId = tutor.getId();

        // 建立學生 (role=1)，供測試非老師建立課程
        com.learning.api.entity.User student = new com.learning.api.entity.User();
        student.setName("Test Student");
        student.setEmail("coursetest.student@example.com");
        student.setPassword("hashedpassword");
        student.setRole(UserRole.STUDENT);
        student.setWallet(0L);
        student = userRepository.save(student);
        savedStudentId = student.getId();

        // 建立基礎課程
        Course course = new Course();
        course.setTutorId(savedTutorId);
        course.setName("Test Course");
        course.setSubject(11);
        course.setDescription("Setup course");
        course.setPrice(500);
        course.setActive(true);
        course = courseRepo.save(course);
        savedCourseId = course.getId();
    }

    // ===================== GET =====================

    @Test
    void getAll_shouldReturnNonEmptyList() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void getById_existingId_shouldReturn200WithCourse() throws Exception {
        mockMvc.perform(get("/api/courses/{id}", savedCourseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCourseId))
                .andExpect(jsonPath("$.name").value("Test Course"))
                .andExpect(jsonPath("$.tutorId").value(savedTutorId))
                .andExpect(jsonPath("$.subject").value(11))
                .andExpect(jsonPath("$.price").value(500))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getById_nonExistingId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/courses/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByTutorId_shouldReturnAllCoursesForTutor() throws Exception {
        mockMvc.perform(get("/api/courses/tutor/{tutorId}", savedTutorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].tutorId").value(savedTutorId));
    }

    @Test
    void getByTutorIdActive_shouldReturnOnlyActiveCourses() throws Exception {
        // 建立一筆下架課程
        Course inactive = new Course();
        inactive.setTutorId(savedTutorId);
        inactive.setName("Inactive Course");
        inactive.setSubject(12);
        inactive.setPrice(300);
        inactive.setActive(false);
        courseRepo.save(inactive);

        mockMvc.perform(get("/api/courses/tutor/{tutorId}/active", savedTutorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].active", everyItem(is(true))));
    }

    // ===================== POST =====================

    @Test
    void post_validRequest_shouldReturn200WithOkMsg() throws Exception {
        Map<String, Object> body = Map.of(
                "tutorId", savedTutorId,
                "name", "New Course",
                "subject", 21,
                "price", 800,
                "active", true
        );

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("ok"));
    }

    @Test
    void post_missingTutorId_shouldReturn400() throws Exception {
        // Map.of 不接受 null，用 HashMap 省略 tutorId
        Map<String, Object> body = new HashMap<>();
        body.put("name", "No Tutor Course");
        body.put("subject", 11);
        body.put("price", 500);
        body.put("active", true);

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("建立失敗"));
    }

    @Test
    void post_emptyName_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "tutorId", savedTutorId,
                "name", "   ",
                "subject", 11,
                "price", 500,
                "active", true
        );

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("建立失敗"));
    }

    @Test
    void post_invalidSubject_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "tutorId", savedTutorId,
                "name", "Course",
                "subject", 99,
                "price", 500,
                "active", true
        );

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("建立失敗"));
    }

    @Test
    void post_zeroPrice_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "tutorId", savedTutorId,
                "name", "Course",
                "subject", 11,
                "price", 0,
                "active", true
        );

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("建立失敗"));
    }

    @Test
    void post_nonTutorUser_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "tutorId", savedStudentId,
                "name", "Course",
                "subject", 11,
                "price", 500,
                "active", true
        );

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("建立失敗"));
    }

    // ===================== PUT =====================

    @Test
    void put_existingId_validRequest_shouldReturn200WithUpdatedCourse() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Updated Course",
                "subject", 12,
                "price", 600,
                "active", false
        );

        mockMvc.perform(put("/api/courses/{id}", savedCourseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Course"))
                .andExpect(jsonPath("$.subject").value(12))
                .andExpect(jsonPath("$.price").value(600))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void put_nonExistingId_shouldReturn404() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Updated Course",
                "subject", 11,
                "price", 500,
                "active", true
        );

        mockMvc.perform(put("/api/courses/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void put_emptyName_shouldReturn400WithMessage() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "  ",
                "subject", 11,
                "price", 500,
                "active", true
        );

        mockMvc.perform(put("/api/courses/{id}", savedCourseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("驗證失敗")));
    }

    @Test
    void put_invalidSubject_shouldReturn400WithMessage() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Valid Name",
                "subject", 99,
                "price", 500,
                "active", true
        );

        mockMvc.perform(put("/api/courses/{id}", savedCourseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("驗證失敗")));
    }

    // ===================== DELETE =====================

    @Test
    void delete_existingId_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/courses/{id}", savedCourseId))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_nonExistingId_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/courses/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_thenGetById_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/courses/{id}", savedCourseId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/courses/{id}", savedCourseId))
                .andExpect(status().isNotFound());
    }
}
