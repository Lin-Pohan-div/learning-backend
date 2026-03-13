package com.learning.api.controller;

import com.learning.api.dto.EmailBookingDTO;
import com.learning.api.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test-email")
public class TestEmailController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/send")
    public String sendEmail(@RequestParam String to,
                            @RequestParam String subject,
                            @RequestParam String text) {
        emailService.sendSimpleEmail(to, subject, text);
        return "Email 已寄出給: " + to;
    }
    @PostMapping("/send-booking")
    public String sendBookingEmail(@RequestBody EmailBookingDTO dto) {

        emailService.sendBookingEmail(dto);

        return "Booking email 處理完成";
    }
}