package com.learning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.api.TestFactory;
import com.learning.api.dto.booking.BookingReq;
import com.learning.api.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules(); // 支援 LocalDate 序列化

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();
    }

    

    private BookingReq makeReq(Long userId, Long courseId, Integer lessonCount) {
        return TestFactory.makeBookingReq(userId, courseId, lessonCount);
    }

    // ── POST /api/bookings ────────────────────────────────────────────────────

    @Test
    void post_validRequest_shouldReturn200() throws Exception {
        when(bookingService.sendBooking(any(BookingReq.class))).thenReturn(true);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeReq(1L, 1L, 5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("建立成功"));
    }

    @Test
    void post_serviceReturnsFalse_shouldReturn400() throws Exception {
        when(bookingService.sendBooking(any(BookingReq.class))).thenReturn(false);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeReq(1L, 1L, 5))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("建立失敗"));
    }

}
