package com.learning.api.controller;

import com.learning.api.annotation.ApiController;
import com.learning.api.dto.booking.BookingReq;
import com.learning.api.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@ApiController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> sendBooking(@RequestBody BookingReq bookingReq) {
        if (!bookingService.sendBooking(bookingReq)) {
            return ResponseEntity.status(400).body(Map.of("msg", "建立失敗"));
        }
        return ResponseEntity.ok(Map.of("msg", "建立成功"));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<?> completeBooking(@PathVariable Long id) {
        String result = bookingService.completeBooking(id);
        if (!"success".equals(result)) {
            return ResponseEntity.status(400).body(Map.of("msg", result));
        }
        return ResponseEntity.ok(Map.of("msg", "課程已完成，教師收入已發放"));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        String result = bookingService.cancelBooking(id);
        if (!"success".equals(result)) {
            return ResponseEntity.status(400).body(Map.of("msg", result));
        }
        return ResponseEntity.ok(Map.of("msg", "課程已取消，退款已退回學生錢包"));
    }
}
