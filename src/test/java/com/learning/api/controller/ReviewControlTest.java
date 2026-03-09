package com.learning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import com.learning.api.entity.Review;
import com.learning.api.repo.ReviewRepository;
import com.learning.api.repo.CourseRepository;
import com.learning.api.repo.UserRepository;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class ReviewControlTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Review savedReview;
    private Long savedUserId;
    private Long savedCourseId;
    private Long savedCourseId2;

    @BeforeEach
    void setUp() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // create a user so fk_reviews_user constraint is satisfied
        com.learning.api.entity.User testUser = new com.learning.api.entity.User();
        testUser.setName("Test User");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("hashedpassword");
        testUser.setRole(1);
        testUser = userRepository.save(testUser);
        savedUserId = testUser.getId();

        // create a tutor user so fk_courses_tutor constraint is satisfied
        com.learning.api.entity.User tutorUser = new com.learning.api.entity.User();
        tutorUser.setName("Test Tutor");
        tutorUser.setEmail("testtutor@example.com");
        tutorUser.setPassword("hashedpassword");
        tutorUser.setRole(2);
        tutorUser = userRepository.save(tutorUser);

        // create a course so fk_reviews_course constraint is satisfied
        com.learning.api.entity.Course testCourse = new com.learning.api.entity.Course();
        testCourse.setTutorId(tutorUser.getId());
        testCourse.setName("Test Course");
        testCourse.setSubject(1);
        testCourse.setLevel(1);
        testCourse.setDescription("Course for testing");
        testCourse.setPrice(500);
        testCourse.setActive(true);
        testCourse = courseRepository.save(testCourse);
        savedCourseId = testCourse.getId();

        com.learning.api.entity.Course testCourse2 = new com.learning.api.entity.Course();
        testCourse2.setTutorId(tutorUser.getId());
        testCourse2.setName("Test Course 2");
        testCourse2.setSubject(1);
        testCourse2.setLevel(1);
        testCourse2.setDescription("Second course for POST testing");
        testCourse2.setPrice(600);
        testCourse2.setActive(true);
        testCourse2 = courseRepository.save(testCourse2);
        savedCourseId2 = testCourse2.getId();

        reviewRepository.deleteAll();

        Review review = new Review();
        review.setUserId(savedUserId);
        review.setCourseId(savedCourseId);
        review.setRating((byte) 4);
        review.setComment("Initial comment");
        savedReview = reviewRepository.save(review);
    }

    // ===================== GET =====================

    @Test
    void getAll_shouldReturnListWithSavedReview() throws Exception {
        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].rating").isNumber());
    }

    @Test
    void getById_existingId_shouldReturn200WithReview() throws Exception {
        mockMvc.perform(get("/api/reviews/{id}", savedReview.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedReview.getId()))
                .andExpect(jsonPath("$.userId").value(savedUserId))
                .andExpect(jsonPath("$.courseId").value(savedCourseId))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Initial comment"));
    }

    @Test
    void getById_nonExistingId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/reviews/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByUserId_shouldReturnMatchingReviews() throws Exception {
        mockMvc.perform(get("/api/reviews/user/{userId}", savedUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].userId").value(savedUserId));
    }

    @Test
    void getByCourseId_shouldReturnMatchingReviews() throws Exception {
        mockMvc.perform(get("/api/reviews/course/{courseId}", savedCourseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].courseId").value(savedCourseId));
    }

    @Test
    void getAverageRating_shouldReturnCourseIdAndAverageRating() throws Exception {
        mockMvc.perform(get("/api/reviews/course/{courseId}/average-rating", savedCourseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(savedCourseId))
                .andExpect(jsonPath("$.averageRating").isNumber());
    }

    // ===================== POST =====================

    @Test
    void post_validRequest_shouldReturn201WithCreatedReview() throws Exception {
        Map<String, Object> body = Map.of(
                "userId", savedUserId,
                "courseId", savedCourseId2,
                "rating", 5,
                "comment", "Excellent session"
        );

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value(savedUserId))
                .andExpect(jsonPath("$.courseId").value(savedCourseId2))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Excellent session"));
    }

    @Test
    void post_missingUserId_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "courseId", savedCourseId,
                "rating", 5
        );

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("userId")));
    }

    @Test
    void post_missingCourseId_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "userId", savedUserId,
                "rating", 3
        );

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("courseId")));
    }

    @Test
    void post_missingRating_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "userId", savedUserId,
                "courseId", savedCourseId,
                "comment", "No rating provided"
        );

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("rating")));
    }

    // ===================== PUT =====================

    @Test
    void put_existingId_shouldReturn200WithUpdatedReview() throws Exception {
        Review updateBody = new Review();
        updateBody.setUserId(savedReview.getUserId());
        updateBody.setCourseId(savedReview.getCourseId());
        updateBody.setRating((byte) 2);
        updateBody.setComment("Updated comment");

        mockMvc.perform(put("/api/reviews/{id}", savedReview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedReview.getId()))
                .andExpect(jsonPath("$.rating").value(2))
                .andExpect(jsonPath("$.comment").value("Updated comment"));
    }

    @Test
    void put_nonExistingId_shouldReturn404() throws Exception {
        Review updateBody = new Review();
        updateBody.setUserId(savedUserId);
        updateBody.setCourseId(savedCourseId);
        updateBody.setRating((byte) 3);
        updateBody.setComment("Update comment");
    
        mockMvc.perform(put("/api/reviews/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isNotFound());
    }

    // ===================== DELETE =====================

    @Test
    void delete_existingId_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/reviews/{id}", savedReview.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_nonExistingId_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/reviews/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_thenGetById_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/reviews/{id}", savedReview.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/reviews/{id}", savedReview.getId()))
                .andExpect(status().isNotFound());
    }
}
