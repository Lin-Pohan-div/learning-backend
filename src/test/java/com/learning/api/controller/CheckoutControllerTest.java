package com.learning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.learning.api.entity.*;
import com.learning.api.repo.*;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class CheckoutControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired private UserRepository userRepo;
    @Autowired private CourseRepo courseRepo;
    @Autowired private TutorScheduleRepo scheduleRepo;
    @Autowired private BookingRepository bookingRepo;
    @Autowired private OrderRepository orderRepo;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private Long studentId;
    private Long poorStudentId;
    private Long courseId;
    private LocalDate nextMonday;
    private static final int TEST_HOUR = 10;

    @BeforeEach
    void setUp() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 建立有錢的學生
        User student = new User();
        student.setName("Rich Student");
        student.setEmail("rich.student@example.com");
        student.setPassword("hashed");
        student.setRole(UserRole.STUDENT);
        student.setWallet(10000L);
        student = userRepo.save(student);
        studentId = student.getId();

        // 建立沒錢的學生
        User poorStudent = new User();
        poorStudent.setName("Poor Student");
        poorStudent.setEmail("poor.student@example.com");
        poorStudent.setPassword("hashed");
        poorStudent.setRole(UserRole.STUDENT);
        poorStudent.setWallet(0L);
        poorStudent = userRepo.save(poorStudent);
        poorStudentId = poorStudent.getId();

        // 建立老師
        User tutor = new User();
        tutor.setName("Test Tutor");
        tutor.setEmail("checkout.tutor@example.com");
        tutor.setPassword("hashed");
        tutor.setRole(UserRole.TUTOR);
        tutor.setWallet(0L);
        tutor = userRepo.save(tutor);
        Long tutorId = tutor.getId();

        // 建立課程
        Course course = new Course();
        course.setTutorId(tutorId);
        course.setName("Checkout Test Course");
        course.setSubject(11);
        course.setPrice(500);
        course.setActive(true);
        course = courseRepo.save(course);
        courseId = course.getId();

        // 建立老師課表 (下週一, 10:00)
        nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        TutorSchedule schedule = new TutorSchedule();
        schedule.setTutorId(tutorId);
        schedule.setWeekday(1); // 星期一
        schedule.setHour(TEST_HOUR);
        schedule.setStatus("available");
        scheduleRepo.save(schedule);
    }

    @Test
    void purchase_validRequest_shouldReturn200() throws Exception {
        Map<String, Object> body = Map.of(
                "studentId", studentId,
                "courseId", courseId,
                "selectedSlots", List.of(Map.of("date", nextMonday.toString(), "hour", TEST_HOUR))
        );

        mockMvc.perform(post("/api/shop/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("購買並預約成功！"));
    }

    @Test
    void purchase_insufficientBalance_shouldReturn402() throws Exception {
        Map<String, Object> body = Map.of(
                "studentId", poorStudentId,
                "courseId", courseId,
                "selectedSlots", List.of(Map.of("date", nextMonday.toString(), "hour", TEST_HOUR))
        );

        mockMvc.perform(post("/api/shop/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().is(402))
                .andExpect(jsonPath("$.msg").value("餘額不足"))
                .andExpect(jsonPath("$.action").value("recharge"));
    }

    @Test
    void purchase_unavailableSlot_shouldReturn400() throws Exception {
        // 使用沒有課表的時段 (23:00)
        Map<String, Object> body = Map.of(
                "studentId", studentId,
                "courseId", courseId,
                "selectedSlots", List.of(Map.of("date", nextMonday.toString(), "hour", 23))
        );

        mockMvc.perform(post("/api/shop/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").isNotEmpty());
    }

    @Test
    void purchase_alreadyBooked_shouldReturn400() throws Exception {
        // 先建立一筆已存在的預約
        Course course = courseRepo.findById(courseId).orElseThrow();
        Order dummyOrder = new Order();
        dummyOrder.setUserId(studentId);
        dummyOrder.setCourseId(courseId);
        dummyOrder.setUnitPrice(course.getPrice());
        dummyOrder.setDiscountPrice(course.getPrice());
        dummyOrder.setLessonCount(1);
        dummyOrder.setLessonUsed(1);
        dummyOrder.setStatus(2);
        dummyOrder = orderRepo.save(dummyOrder);

        Bookings existing = new Bookings();
        existing.setOrderId(dummyOrder.getId());
        existing.setTutorId(course.getTutorId());
        existing.setStudentId(studentId);
        existing.setDate(nextMonday);
        existing.setHour(TEST_HOUR);
        existing.setStatus((byte) 1);
        bookingRepo.save(existing);

        Map<String, Object> body = Map.of(
                "studentId", studentId,
                "courseId", courseId,
                "selectedSlots", List.of(Map.of("date", nextMonday.toString(), "hour", TEST_HOUR))
        );

        mockMvc.perform(post("/api/shop/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").isNotEmpty());
    }
}
