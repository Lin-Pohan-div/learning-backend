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
import com.learning.api.entity.Booking;
import com.learning.api.entity.ChatMessage;
import com.learning.api.repo.BookingRepository;
import com.learning.api.repo.ChatMessageRepository;
import com.learning.api.repo.CourseRepository;
import com.learning.api.repo.UserRepository;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class ChatMessageControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Booking testBooking;
    private ChatMessage savedMessage;

    @BeforeEach
    void setUp() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // create a user so fk_courses_tutor constraint is satisfied
        com.learning.api.entity.User testUser = new com.learning.api.entity.User();
        testUser.setName("Test Tutor");
        testUser.setEmail("testtutor@example.com");
        testUser.setPassword("hashedpassword");
        testUser.setRole(2);
        testUser = userRepository.save(testUser);

        // create a course so fk_bookings_course constraint is satisfied
        com.learning.api.entity.Course testCourse = new com.learning.api.entity.Course();
        testCourse.setTutorId(testUser.getId());
        testCourse.setName("Test Course");
        testCourse.setSubject(1);
        testCourse.setLevel(1);
        testCourse.setDescription("Course for testing");
        testCourse.setPrice(500);
        testCourse.setActive(true);
        testCourse = courseRepository.save(testCourse);

        testBooking = new Booking();
        testBooking.setUserId(1L);
        testBooking.setCourseId(testCourse.getId());
        testBooking.setUnitPrice(100);
        testBooking.setLessonCount(1);
        testBooking.setLessonused(0);
        testBooking.setStatus((byte)1);
        testBooking = bookingRepository.save(testBooking);

        ChatMessage msg = new ChatMessage();
        msg.setBookingId(testBooking.getId());
        msg.setRole((byte) 1);
        msg.setMessage("Initial message");
        savedMessage = chatMessageRepository.save(msg);
    }

    // ===================== GET =====================

    @Test
    void getByBookingId_existingBooking_shouldReturnMessages() throws Exception {
        mockMvc.perform(get("/api/chat-messages/booking/{bookingId}", testBooking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].bookingId").value(testBooking.getId()))
                .andExpect(jsonPath("$[0].role").value(1))
                .andExpect(jsonPath("$[0].message").value("Initial message"));
    }

    @Test
    void getByBookingId_noMessages_shouldReturnEmptyList() throws Exception {
        chatMessageRepository.deleteAllInBatch();

        mockMvc.perform(get("/api/chat-messages/booking/{bookingId}", testBooking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getByBookingId_messagesOrderedByCreatedAtAsc() throws Exception {
        ChatMessage msg2 = new ChatMessage();
        msg2.setBookingId(testBooking.getId());
        msg2.setRole((byte) 2);
        msg2.setMessage("Second message");
        chatMessageRepository.save(msg2);

        mockMvc.perform(get("/api/chat-messages/booking/{bookingId}", testBooking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].message").value("Initial message"))
                .andExpect(jsonPath("$[1].message").value("Second message"));
    }

    // ===================== POST =====================

    @Test
    void post_validRequest_studentRole_shouldReturn201() throws Exception {
        Map<String, Object> body = Map.of(
                "bookingId", testBooking.getId(),
                "role", 1,
                "message", "Hello tutor"
        );

        mockMvc.perform(post("/api/chat-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.bookingId").value(testBooking.getId()))
                .andExpect(jsonPath("$.role").value(1))
                .andExpect(jsonPath("$.message").value("Hello tutor"));
    }

    @Test
    void post_validRequest_tutorRole_shouldReturn201() throws Exception {
        Map<String, Object> body = Map.of(
                "bookingId", testBooking.getId(),
                "role", 2,
                "message", "Hello student"
        );

        mockMvc.perform(post("/api/chat-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value(2))
                .andExpect(jsonPath("$.message").value("Hello student"));
    }

    @Test
    void post_missingBookingId_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "role", 1,
                "message", "Hello"
        );

        mockMvc.perform(post("/api/chat-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Booking ID")));
    }

    @Test
    void post_missingRole_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "bookingId", testBooking.getId(),
                "message", "Hello"
        );

        mockMvc.perform(post("/api/chat-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Role")));
    }

    @Test
    void post_emptyMessage_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "bookingId", testBooking.getId(),
                "role", 1,
                "message", "   "
        );

        mockMvc.perform(post("/api/chat-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("消息內容")));
    }

    @Test
    void post_nonExistingBookingId_shouldReturn404() throws Exception {
        Map<String, Object> body = Map.of(
                "bookingId", 999999,
                "role", 1,
                "message", "Hello"
        );

        mockMvc.perform(post("/api/chat-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    // ===================== PUT =====================

    @Test
    void put_existingId_shouldReturn200WithUpdatedMessage() throws Exception {
        Map<String, String> body = Map.of("message", "Updated message content");

        mockMvc.perform(put("/api/chat-messages/{id}", savedMessage.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedMessage.getId()))
                .andExpect(jsonPath("$.message").value("Updated message content"))
                .andExpect(jsonPath("$.bookingId").value(testBooking.getId()));
    }

    @Test
    void put_nonExistingId_shouldReturn404() throws Exception {
        Map<String, String> body = Map.of("message", "Updated");

        mockMvc.perform(put("/api/chat-messages/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void put_emptyMessage_shouldReturn400() throws Exception {
        Map<String, String> body = Map.of("message", "  ");

        mockMvc.perform(put("/api/chat-messages/{id}", savedMessage.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("消息內容")));
    }

    // ===================== DELETE =====================

    @Test
    void delete_existingId_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/chat-messages/{id}", savedMessage.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_nonExistingId_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/chat-messages/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_thenGetByBookingId_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(delete("/api/chat-messages/{id}", savedMessage.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/chat-messages/booking/{bookingId}", testBooking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
