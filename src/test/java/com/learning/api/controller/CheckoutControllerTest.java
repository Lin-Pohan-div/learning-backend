package com.learning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.api.dto.CheckoutReq;
import com.learning.api.service.CheckoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock private CheckoutService checkoutService;

    @InjectMocks
    private CheckoutController checkoutController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules(); // 支援 LocalDate 序列化

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(checkoutController).build();
    }

    

    private CheckoutReq makeReq(Long studentId, Long courseId) {
        CheckoutReq req = new CheckoutReq();
        req.setStudentId(studentId);
        req.setCourseId(courseId);
        CheckoutReq.Slot slot = new CheckoutReq.Slot();
        slot.setDate(LocalDate.of(2026, 3, 20));
        slot.setHour(10);
        req.setSelectedSlots(List.of(slot));
        return req;
    }

    // ── POST /api/shop/purchase ───────────────────────────────────────────────

    @Test
    void purchase_success_returns200() throws Exception {
        when(checkoutService.processPurchase(any(CheckoutReq.class))).thenReturn("success");

        mockMvc.perform(post("/api/shop/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeReq(1L, 1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("購買並預約成功！"));
    }

    @Test
    void purchase_insufficientBalance_returns402() throws Exception {
        when(checkoutService.processPurchase(any(CheckoutReq.class))).thenReturn("餘額不足");

        mockMvc.perform(post("/api/shop/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeReq(1L, 1L))))
                .andExpect(status().is(402))
                .andExpect(jsonPath("$.msg").value("餘額不足"))
                .andExpect(jsonPath("$.action").value("recharge"));
    }

    @Test
    void purchase_slotUnavailable_returns400() throws Exception {
        when(checkoutService.processPurchase(any(CheckoutReq.class)))
                .thenReturn("時段 2026-03-20 10:00 已不開放");

        mockMvc.perform(post("/api/shop/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeReq(1L, 1L))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").exists());
    }

}
