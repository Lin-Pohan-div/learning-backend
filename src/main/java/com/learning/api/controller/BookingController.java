package com.learning.api.controller;

import com.learning.api.dto.BookingReq;
import com.learning.api.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> sendBooking(@RequestBody BookingReq bookingReq){

        if (!bookingService.sendBooking(bookingReq)) {
            return ResponseEntity.status(400).body(Map.of("msg", "建立失敗"));
        }

        return ResponseEntity.ok(Map.of("msg", "建立成功"));
    }
}
