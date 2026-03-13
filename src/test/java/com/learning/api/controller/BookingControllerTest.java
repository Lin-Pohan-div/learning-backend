package com.learning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.api.entity.Course;
import com.learning.api.entity.User;
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

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class BookingControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private Long testUserId;
    private Long testCourseId;
    private Long inactiveCourseId;

    @BeforeEach
    void setUp() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        User user = new User();
        user.setName("Test Student");
        user.setEmail("student_booking@example.com");
        user.setPassword("hashedpassword");
        user.setRole(1);
        user.setWallet(0L);
        user = userRepository.save(user);
        testUserId = user.getId();

        Course activeCourse = new Course();
        activeCourse.setTutorId(user.getId());
        activeCourse.setName("Active Course");
        activeCourse.setSubject(11);
        activeCourse.setLevel(1);
        activeCourse.setPrice(500);
        activeCourse.setActive(true);
        activeCourse = courseRepo.save(activeCourse);
        testCourseId = activeCourse.getId();

        Course inactiveCourse = new Course();
        inactiveCourse.setTutorId(user.getId());
        inactiveCourse.setName("Inactive Course");
        inactiveCourse.setSubject(11);
        inactiveCourse.setLevel(1);
        inactiveCourse.setPrice(500);
        inactiveCourse.setActive(false);
        inactiveCourse = courseRepo.save(inactiveCourse);
        inactiveCourseId = inactiveCourse.getId();
    }

    @Test
    void post_validRequest_shouldReturn200() throws Exception {
        Map<String, Object> body = Map.of(
                "userId", testUserId,
                "courseId", testCourseId,
                "lessonCount", 5
        );

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("建立成功"));
    }

    @Test
    void post_10Lessons_shouldReturn200() throws Exception {
        Map<String, Object> body = Map.of(
                "userId", testUserId,
                "courseId", testCourseId,
                "lessonCount", 10
        );

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("建立成功"));
    }

    @Test
    void post_nullUserId_shouldReturn400() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", null);
        body.put("courseId", testCourseId);
        body.put("lessonCount", 5);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("建立失敗"));
    }

    @Test
    void post_nullCourseId_shouldReturn400() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", testUserId);
        body.put("courseId", null);
        body.put("lessonCount", 5);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("建立失敗"));
    }

    @Test
    void post_nullLessonCount_shouldReturn400() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", testUserId);
        body.put("courseId", testCourseId);
        body.put("lessonCount", null);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("建立失敗"));
    }

    @Test
    void post_lessonCountZero_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "userId", testUserId,
                "courseId", testCourseId,
                "lessonCount", 0
        );

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("建立失敗"));
    }

    @Test
    void post_nonExistentUser_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "userId", 999999L,
                "courseId", testCourseId,
                "lessonCount", 5
        );

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("建立失敗"));
    }

    @Test
    void post_nonExistentCourse_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "userId", testUserId,
                "courseId", 999999L,
                "lessonCount", 5
        );

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("建立失敗"));
    }

    @Test
    void post_inactiveCourse_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "userId", testUserId,
                "courseId", inactiveCourseId,
                "lessonCount", 5
        );

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("建立失敗"));
    }
}
