package com.learning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.api.entity.Bookings;
import com.learning.api.entity.Course;
import com.learning.api.entity.LessonFeedback;
import com.learning.api.entity.Order;
import com.learning.api.entity.User;
import com.learning.api.repo.BookingRepository;
import com.learning.api.repo.CourseRepo;
import com.learning.api.repo.LessonFeedbackRepository;
import com.learning.api.repo.OrderRepository;
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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class TutorFeedbackControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private LessonFeedbackRepository feedbackRepo;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Long testBookingId;

    @BeforeEach
    void setUp() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 建立使用者
        User tutor = new User();
        tutor.setName("Feedback Tutor");
        tutor.setEmail("feedback.tutor@example.com");
        tutor.setPassword("hashed");
        tutor.setRole(UserRole.TUTOR);
        tutor.setWallet(0L);
        tutor = userRepo.save(tutor);

        User student = new User();
        student.setName("Feedback Student");
        student.setEmail("feedback.student@example.com");
        student.setPassword("hashed");
        student.setRole(UserRole.STUDENT);
        student.setWallet(0L);
        student = userRepo.save(student);

        // 建立課程與訂單（order_id 為 NOT NULL）
        Course course = new Course();
        course.setTutorId(tutor.getId());
        course.setName("Feedback Test Course");
        course.setSubject(11);
        course.setPrice(500);
        course.setActive(true);
        course = courseRepo.save(course);

        Order order = new Order();
        order.setUserId(student.getId());
        order.setCourseId(course.getId());
        order.setUnitPrice(500);
        order.setDiscountPrice(500);
        order.setLessonCount(1);
        order.setLessonUsed(1);
        order.setStatus(2);
        order = orderRepo.save(order);

        // 建立預約紀錄
        Bookings booking = new Bookings();
        booking.setOrderId(order.getId());
        booking.setTutorId(tutor.getId());
        booking.setStudentId(student.getId());
        booking.setDate(LocalDate.now().minusDays(1));
        booking.setHour(10);
        booking.setStatus((byte) 2);
        booking = bookingRepo.save(booking);
        testBookingId = booking.getId();
    }

    private Map<String, Object> validFeedbackBody(Long bookingId, int rating) {
        Map<String, Object> body = new HashMap<>();
        body.put("bookingId", bookingId);
        body.put("rating", rating);
        body.put("focusScore", 4);
        body.put("comprehensionScore", 4);
        body.put("confidenceScore", 4);
        body.put("comment", "Good lesson");
        return body;
    }

    // ===================== POST /api/teacher/feedbacks =====================

    @Test
    void submitFeedback_validRequest_shouldReturn200() throws Exception {
        mockMvc.perform(post("/api/teacher/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFeedbackBody(testBookingId, 4))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("課後回饋送出成功！家長將會收到通知。"));
    }

    @Test
    void submitFeedback_ratingZero_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/teacher/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFeedbackBody(testBookingId, 0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("評分必須介於 1 到 5 之間"));
    }

    @Test
    void submitFeedback_ratingSix_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/teacher/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFeedbackBody(testBookingId, 6))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("評分必須介於 1 到 5 之間"));
    }

    @Test
    void submitFeedback_duplicate_shouldReturn400() throws Exception {
        // 先送出一筆回饋
        LessonFeedback existing = new LessonFeedback();
        existing.setBookingId(testBookingId);
        existing.setRating(3);
        existing.setFocusScore(3);
        existing.setComprehensionScore(3);
        existing.setConfidenceScore(3);
        feedbackRepo.save(existing);

        mockMvc.perform(post("/api/teacher/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFeedbackBody(testBookingId, 4))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("這堂課已經填寫過回饋囉！"));
    }
}
