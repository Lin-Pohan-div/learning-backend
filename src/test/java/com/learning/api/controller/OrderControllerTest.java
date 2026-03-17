package com.learning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.api.TestFactory;
import com.learning.api.dto.OrderDto;
import com.learning.api.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
    }

    

    private OrderDto.Req makeOrderReq(Long userId, Long courseId, Integer lessonCount) {
        return TestFactory.makeOrderReq(userId, courseId, lessonCount);
    }

    private OrderDto.Resp makeOrderResp(Long id, Long userId, Long courseId, int status) {
        return TestFactory.makeOrderResp(id, userId, courseId, status);
    }

    // ── POST /api/orders — 建立訂單 ───────────────────────────────────────────

    @Test
    void createOrder_validRequest_shouldReturn200() throws Exception {
        when(orderService.createOrder(any(OrderDto.Req.class))).thenReturn(true);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeOrderReq(1L, 1L, 5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("訂單建立成功"));
    }

    @Test
    void createOrder_nonExistentUser_shouldReturn400() throws Exception {
        when(orderService.createOrder(any(OrderDto.Req.class))).thenReturn(false);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeOrderReq(999999L, 1L, 5))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("建立訂單失敗"));
    }

    // ── GET /api/orders/{id} — 查詢單筆訂單 ──────────────────────────────────

    @Test
    void getOrder_existingId_shouldReturn200() throws Exception {
        OrderDto.Resp resp = makeOrderResp(1L, 2L, 3L, 1);
        when(orderService.getOrderById(1L)).thenReturn(resp);

        mockMvc.perform(get("/api/orders/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.courseId").value(3))
                .andExpect(jsonPath("$.status").value(1));
    }

    @Test
    void getOrder_nonExistentId_shouldReturn404() throws Exception {
        when(orderService.getOrderById(999999L)).thenReturn(null);

        mockMvc.perform(get("/api/orders/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.msg").value("訂單不存在"));
    }

    // ── GET /api/orders/user/{userId} — 查詢使用者所有訂單 ───────────────────

    @Test
    void getOrdersByUser_existingUser_shouldReturnList() throws Exception {
        List<OrderDto.Resp> list = List.of(
                makeOrderResp(1L, 2L, 1L, 1),
                makeOrderResp(2L, 2L, 1L, 2),
                makeOrderResp(3L, 2L, 1L, 3)
        );
        when(orderService.getOrdersByUserId(2L)).thenReturn(list);

        mockMvc.perform(get("/api/orders/user/{userId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void getOrdersByUser_noOrders_shouldReturnEmptyList() throws Exception {
        when(orderService.getOrdersByUserId(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/orders/user/{userId}", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── PUT /api/orders/{id} — 更新訂單 ──────────────────────────────────────

    @Test
    void updateOrder_lessonCount_shouldReturn200() throws Exception {
        when(orderService.updateOrder(eq(1L), any(OrderDto.UpdateReq.class))).thenReturn(true);

        OrderDto.UpdateReq req = new OrderDto.UpdateReq();
        req.setLessonCount(8);

        mockMvc.perform(put("/api/orders/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("訂單更新成功"));
    }

    @Test
    void updateOrder_completeStatus_shouldReturn400() throws Exception {
        when(orderService.updateOrder(eq(3L), any(OrderDto.UpdateReq.class))).thenReturn(false);

        OrderDto.UpdateReq req = new OrderDto.UpdateReq();
        req.setLessonCount(8);

        mockMvc.perform(put("/api/orders/{id}", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("訂單更新失敗"));
    }

    // ── PATCH /api/orders/{id}/status — 更新訂單狀態 ─────────────────────────

    @Test
    void updateStatus_pendingToDeal_shouldReturn200() throws Exception {
        when(orderService.updateStatus(eq(1L), any(OrderDto.StatusReq.class))).thenReturn(true);

        OrderDto.StatusReq req = new OrderDto.StatusReq();
        req.setStatus(2);

        mockMvc.perform(patch("/api/orders/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("狀態更新成功"));
    }

    @Test
    void updateStatus_backward_shouldReturn400() throws Exception {
        when(orderService.updateStatus(eq(2L), any(OrderDto.StatusReq.class))).thenReturn(false);

        OrderDto.StatusReq req = new OrderDto.StatusReq();
        req.setStatus(1);

        mockMvc.perform(patch("/api/orders/{id}/status", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("狀態更新失敗"));
    }

    // ── DELETE /api/orders/{id} — 取消訂單 ───────────────────────────────────

    @Test
    void cancelOrder_pendingStatus_shouldReturn200() throws Exception {
        when(orderService.cancelOrder(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/orders/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("訂單已取消"));
    }

    @Test
    void cancelOrder_dealStatus_shouldReturn400() throws Exception {
        when(orderService.cancelOrder(2L)).thenReturn(false);

        mockMvc.perform(delete("/api/orders/{id}", 2L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("取消失敗，僅 pending 訂單可取消"));
    }


}
