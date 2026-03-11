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
import com.learning.api.entity.Order;
import com.learning.api.entity.ChatMessage;
import com.learning.api.repo.OrderRepository;
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
    private OrderRepository bookingRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Order testBooking;
    private ChatMessage savedMessage;

    @BeforeEach
    void setUp() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();


        com.learning.api.entity.User testUser = new com.learning.api.entity.User();
        testUser.setName("Test Tutor");
        testUser.setEmail("testtutor@example.com");
        testUser.setPassword("hashedpassword");
        testUser.setRole(2);
        testUser.setWallet(0L);
        testUser = userRepository.save(testUser);


        com.learning.api.entity.Course testCourse = new com.learning.api.entity.Course();
        testCourse.setTutorId(testUser.getId());
        testCourse.setName("Test Course");
        testCourse.setSubject(1);
        testCourse.setLevel(1);
        testCourse.setDescription("Course for testing");
        testCourse.setPrice(500);
        testCourse.setActive(true);
        testCourse = courseRepository.save(testCourse);

        testBooking = new Order();
        testBooking.setUserId(testUser.getId());
        testBooking.setCourseId(testCourse.getId());
        testBooking.setUnitPrice(100);
        testBooking.setDiscountPrice(100);
        testBooking.setLessonCount(1);
        testBooking.setLessonUsed(0);
        testBooking.setStatus(1);
        testBooking = bookingRepository.save(testBooking);

        ChatMessage msg = new ChatMessage();
        msg.setOrderId(testBooking.getId());
        msg.setRole((Integer) 1);
        msg.setMessage("Initial message");
        savedMessage = chatMessageRepository.save(msg);
    }

    // ===================== GET =====================

    @Test
    void getByBookingId_existingBooking_shouldReturnMessages() throws Exception {
        mockMvc.perform(get("/api/chatMessage/booking/{bookingId}", testBooking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].orderId").value(testBooking.getId()))
                .andExpect(jsonPath("$[0].role").value(1))
                .andExpect(jsonPath("$[0].message").value("Initial message"));
    }

    @Test
    void getByBookingId_noMessages_shouldReturnEmptyList() throws Exception {
        chatMessageRepository.deleteAllInBatch();

        mockMvc.perform(get("/api/chatMessage/booking/{bookingId}", testBooking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getByBookingId_messagesOrderedByCreatedAtAsc() throws Exception {
        ChatMessage msg2 = new ChatMessage();
        msg2.setOrderId(testBooking.getId());
        msg2.setRole((Integer) 2);
        msg2.setMessage("Second message");
        chatMessageRepository.save(msg2);

        mockMvc.perform(get("/api/chatMessage/booking/{bookingId}", testBooking.getId()))
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

        mockMvc.perform(post("/api/chatMessage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.orderId").value(testBooking.getId()))
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

        mockMvc.perform(post("/api/chatMessage")
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

        mockMvc.perform(post("/api/chatMessage")
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

        mockMvc.perform(post("/api/chatMessage")
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

        mockMvc.perform(post("/api/chatMessage")
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

        mockMvc.perform(post("/api/chatMessage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    // ===================== POST - 媒體訊息 =====================

    @Test
    void post_stickerMessage_shouldReturn201() throws Exception {
        Map<String, Object> body = Map.of(
                "bookingId", testBooking.getId(),
                "role", 1,
                "messageType", 2,
                "mediaUrl", "https://example.com/stickers/001.png"
        );

        mockMvc.perform(post("/api/chatMessage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.messageType").value(2))
                .andExpect(jsonPath("$.mediaUrl").value("https://example.com/stickers/001.png"));
    }

    @Test
    void post_voiceMessage_shouldReturn201() throws Exception {
        Map<String, Object> body = Map.of(
                "bookingId", testBooking.getId(),
                "role", 1,
                "messageType", 3,
                "mediaUrl", "https://example.com/audio/001.mp3"
        );

        mockMvc.perform(post("/api/chatMessage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.messageType").value(3))
                .andExpect(jsonPath("$.mediaUrl").value("https://example.com/audio/001.mp3"));
    }

    @Test
    void post_imageMessage_shouldReturn201() throws Exception {
        Map<String, Object> body = Map.of(
                "bookingId", testBooking.getId(),
                "role", 2,
                "messageType", 4,
                "mediaUrl", "https://example.com/images/001.jpg"
        );

        mockMvc.perform(post("/api/chatMessage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.messageType").value(4))
                .andExpect(jsonPath("$.mediaUrl").value("https://example.com/images/001.jpg"));
    }

    @Test
    void post_videoMessage_shouldReturn201() throws Exception {
        Map<String, Object> body = Map.of(
                "bookingId", testBooking.getId(),
                "role", 2,
                "messageType", 5,
                "mediaUrl", "https://example.com/videos/001.mp4"
        );

        mockMvc.perform(post("/api/chatMessage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.messageType").value(5))
                .andExpect(jsonPath("$.mediaUrl").value("https://example.com/videos/001.mp4"));
    }

    @Test
    void post_stickerWithoutMediaUrl_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "bookingId", testBooking.getId(),
                "role", 1,
                "messageType", 2
        );

        mockMvc.perform(post("/api/chatMessage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("貼圖")));
    }

    @Test
    void post_invalidMessageType_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "bookingId", testBooking.getId(),
                "role", 1,
                "messageType", 99,
                "mediaUrl", "https://example.com/something"
        );

        mockMvc.perform(post("/api/chatMessage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ===================== PUT =====================

    @Test
    void put_existingId_shouldReturn200WithUpdatedMessage() throws Exception {
        Map<String, String> body = Map.of("message", "Updated message content");

        mockMvc.perform(put("/api/chatMessage/{id}", savedMessage.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedMessage.getId()))
                .andExpect(jsonPath("$.message").value("Updated message content"))
                .andExpect(jsonPath("$.orderId").value(testBooking.getId()));
    }

    @Test
    void put_nonExistingId_shouldReturn404() throws Exception {
        Map<String, String> body = Map.of("message", "Updated");

        mockMvc.perform(put("/api/chatMessage/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void put_emptyMessage_shouldReturn400() throws Exception {
        Map<String, String> body = Map.of("message", "  ");

        mockMvc.perform(put("/api/chatMessage/{id}", savedMessage.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("消息內容")));
    }

    // ===================== DELETE =====================

    @Test
    void delete_existingId_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/chatMessage/{id}", savedMessage.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_nonExistingId_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/chatMessage/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_thenGetByBookingId_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(delete("/api/chatMessage/{id}", savedMessage.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/chatMessage/booking/{bookingId}", testBooking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
