package com.learning.api.controller;

import com.learning.api.annotation.ApiController;
import com.learning.api.dto.BookingDTO;
import com.learning.api.dto.BookingReq;
import com.learning.api.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@ApiController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<?> getTutorBookings(@PathVariable Long tutorId) {
        List<BookingDTO> bookings = bookingService.getTutorBookings(tutorId);
        return ResponseEntity.ok(bookings);
    }
}
