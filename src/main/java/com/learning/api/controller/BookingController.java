package com.learning.api.controller;

import com.learning.api.dto.BookingReq;
import com.learning.api.entity.Booking;
import com.learning.api.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<?> getTutorBookings(@PathVariable Long tutorId) {
        List<Booking> bookings = bookingService.getTutorBookings(tutorId);
        return ResponseEntity.ok(bookings);
    }
}
