package com.learning.api.controller;

import com.learning.api.dto.auth.UserResp;
import com.learning.api.entity.Booking;
import com.learning.api.entity.User;
import com.learning.api.security.SecurityUser;
import com.learning.api.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/student")
public class StudentController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/profile")
    public ResponseEntity<UserResp> getProfile(@AuthenticationPrincipal SecurityUser securityUser) {
        User user = securityUser.getUser();
        UserResp resp = new UserResp(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBirthday(),
                user.getRole(),
                user.getWallet(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getBookings(@AuthenticationPrincipal SecurityUser securityUser) {
        Long studentId = securityUser.getUser().getId();
        return ResponseEntity.ok(bookingService.getTutorBookings(studentId));
    }
}
