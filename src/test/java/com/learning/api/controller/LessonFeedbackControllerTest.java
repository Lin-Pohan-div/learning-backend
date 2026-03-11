package com.learning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.learning.api.entity.Bookings;
import com.learning.api.entity.LessonFeedback;
import com.learning.api.entity.Order;
import com.learning.api.repo.BookingRepository;
import com.learning.api.repo.CourseRepository;
import com.learning.api.repo.LessonFeedbackRepository;
import com.learning.api.repo.OrderRepository;
import com.learning.api.repo.UserRepository;

import jakarta.transaction.Transactional;
import java.sql.Date;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
public class LessonFeedbackControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private LessonFeedbackRepository lessonFeedbackRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private LessonFeedback savedFeedback;
    private Long savedBookingId;

    @BeforeEach
    void setUp() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        lessonFeedbackRepository.deleteAllInBatch();

        // Create users for tutor and student FK constraints
        com.learning.api.entity.User tutor = new com.learning.api.entity.User();
        tutor.setName("Test Tutor");
        tutor.setEmail("tutor_feedback@example.com");
        tutor.setPassword("hashedpassword");
        tutor.setRole(2);
        tutor.setWallet(0L);
        tutor = userRepository.save(tutor);

        com.learning.api.entity.User student = new com.learning.api.entity.User();
        student.setName("Test Student");
        student.setEmail("student_feedback@example.com");
        student.setPassword("hashedpassword");
        student.setRole(1);
        student.setWallet(0L);
        student = userRepository.save(student);

        // Create a course so orders.course_id FK constraint is satisfied
        com.learning.api.entity.Course course = new com.learning.api.entity.Course();
        course.setTutorId(tutor.getId());
        course.setName("Test Course");
        course.setSubject(1);
        course.setLevel(1);
        course.setDescription("Course for feedback testing");
        course.setPrice(500);
        course.setActive(true);
        course = courseRepository.save(course);

        // Create an order so bookings.order_id NOT NULL constraint is satisfied
        Order order = new Order();
        order.setUserId(student.getId());
        order.setCourseId(course.getId());
        order.setUnitPrice(500);
        order.setDiscountPrice(500);
        order.setLessonCount(1);
        order.setLessonUsed(0);
        order.setStatus(1);
        order = orderRepo.save(order);

        // Create a booking so fk_feedback_lesson constraint is satisfied
        Bookings booking = new Bookings();
        booking.setOrderId(order.getId());
        booking.setTutorId(tutor.getId());
        booking.setStudentId(student.getId());
        booking.setDate(Date.valueOf("2025-01-01"));
        booking.setHour((byte) 10);
        booking.setStatus((byte) 1);
        booking = bookingRepository.save(booking);
        savedBookingId = booking.getId();

        LessonFeedback feedback = new LessonFeedback();
        feedback.setBookingId(savedBookingId);
        feedback.setRating((byte) 4);
        feedback.setComment("Initial feedback");
        savedFeedback = lessonFeedbackRepository.save(feedback);
    }

    // ===================== GET ALL =====================

    @Test
    void getAll_shouldReturnListWithSavedFeedback() throws Exception {
        mockMvc.perform(get("/api/Feedbacks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].id", hasItem(savedFeedback.getId().intValue())))
                .andExpect(jsonPath("$[*].rating", hasItem(4)))
                .andExpect(jsonPath("$[*].comment", hasItem("Initial feedback")));
    }

    // ===================== GET BY ID =====================

    @Test
    void getById_existingId_shouldReturn200WithFeedback() throws Exception {
        mockMvc.perform(get("/api/Feedbacks/{id}", savedFeedback.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedFeedback.getId()))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Initial feedback"));
    }

    @Test
    void getById_nonExistingId_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/Feedbacks/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    // ===================== GET BY LESSON ID =====================

    @Test
    void getByLessonId_noFeedbacks_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/Feedbacks/lesson/{lessonId}", 999999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ===================== GET AVERAGE RATING =====================

    @Test
    void getAverageRating_noFeedbacks_shouldReturnZero() throws Exception {
        mockMvc.perform(get("/api/Feedbacks/lesson/{lessonId}/average-rating", 999999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(999999))
                .andExpect(jsonPath("$.averageRating").value(0.0));
    }

    // ===================== POST =====================

    @Test
    void post_validRequest_shouldReturn201() throws Exception {
        Map<String, Object> body = Map.of(
                "bookingId", savedBookingId,
                "rating", 5,
                "comment", "Great lesson"
        );

        mockMvc.perform(post("/api/Feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Great lesson"));
    }

    @Test
    void post_missingLessonId_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "rating", 3,
                "comment", "Good lesson"
        );

        mockMvc.perform(post("/api/Feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_missingRating_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "bookingId", savedBookingId,
                "comment", "Good lesson"
        );

        mockMvc.perform(post("/api/Feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_ratingBelowMin_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "bookingId", savedBookingId,
                "rating", 0,
                "comment", "Bad rating"
        );

        mockMvc.perform(post("/api/Feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_ratingAboveMax_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "bookingId", savedBookingId,
                "rating", 6,
                "comment", "Over max rating"
        );

        mockMvc.perform(post("/api/Feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ===================== PUT =====================

    @Test
    void put_existingId_shouldReturn200WithUpdatedFeedback() throws Exception {
        LessonFeedback updateBody = new LessonFeedback();
        updateBody.setRating((byte) 2);
        updateBody.setComment("Updated comment");

        mockMvc.perform(put("/api/Feedbacks/{id}", savedFeedback.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedFeedback.getId()))
                .andExpect(jsonPath("$.rating").value(2))
                .andExpect(jsonPath("$.comment").value("Updated comment"));
    }

    @Test
    void put_nonExistingId_shouldReturn404() throws Exception {
        LessonFeedback updateBody = new LessonFeedback();
        updateBody.setRating((byte) 3);
        updateBody.setComment("Updated comment");

        mockMvc.perform(put("/api/Feedbacks/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isNotFound());
    }

    // ===================== DELETE =====================

    @Test
    void delete_existingId_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/Feedbacks/{id}", savedFeedback.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_nonExistingId_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/Feedbacks/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_thenGetById_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/Feedbacks/{id}", savedFeedback.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/Feedbacks/{id}", savedFeedback.getId()))
                .andExpect(status().isNotFound());
    }
}
