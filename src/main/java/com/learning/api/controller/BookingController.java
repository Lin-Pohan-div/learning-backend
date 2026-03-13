package com.learning.api.controller;

import com.learning.api.annotation.ApiController;
import com.learning.api.dto.BookingReq;
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
            return ResponseEntity.status(400).body(Map.of("message", "建立失敗"));
        }
        return ResponseEntity.ok(Map.of("message", "建立成功"));
    }
}
