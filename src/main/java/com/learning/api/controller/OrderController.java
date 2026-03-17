package com.learning.api.controller;

import com.learning.api.annotation.ApiController;
import com.learning.api.dto.OrderDto;
import com.learning.api.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@ApiController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 新增訂單
    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderDto.Req req) {
        if (!orderService.createOrder(req)) {
            return ResponseEntity.status(400).body(Map.of("msg", "建立訂單失敗"));
        }
        return ResponseEntity.ok(Map.of("msg", "訂單建立成功"));
    }

    // 修改訂單 (lessonCount / lessonUsed)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderDto.UpdateReq req) {

        if (!orderService.updateOrder(id, req)) {
            return ResponseEntity.status(400).body(Map.of("msg", "訂單更新失敗"));
        }
        return ResponseEntity.ok(Map.of("msg", "訂單更新成功"));
    }

    // 查詢單一訂單
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        OrderDto.Resp resp = orderService.getOrderById(id);
        if (resp == null) {
            return ResponseEntity.status(404).body(Map.of("msg", "訂單不存在"));
        }
        return ResponseEntity.ok(resp);
    }

    // 查詢使用者所有訂單
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUser(@PathVariable Long userId) {
        List<OrderDto.Resp> list = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(list);
    }

    // 更新訂單狀態 (pending→deal→complete)
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderDto.StatusReq req) {

        if (!orderService.updateStatus(id, req)) {
            return ResponseEntity.status(400).body(Map.of("msg", "狀態更新失敗"));
        }
        return ResponseEntity.ok(Map.of("msg", "狀態更新成功"));
    }

    // 取消訂單 (僅限 pending 狀態)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        if (!orderService.cancelOrder(id)) {
            return ResponseEntity.status(400).body(Map.of("msg", "取消失敗，僅 pending 訂單可取消"));
        }
        return ResponseEntity.ok(Map.of("msg", "訂單已取消"));
    }

}
