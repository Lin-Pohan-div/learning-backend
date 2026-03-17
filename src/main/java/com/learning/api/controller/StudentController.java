package com.learning.api.controller;

import com.learning.api.annotation.ApiController;
import com.learning.api.repo.BookingRepository;
import com.learning.api.repo.WalletLogRepository;
import com.learning.api.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 學生專屬 API — 需要 STUDENT role（SecurityConfig 已設定 /api/student/** → STUDENT）
 */
@ApiController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final BookingRepository bookingRepo;
    private final WalletLogRepository walletLogRepo;

    /** 查詢自己的所有預約 */
    @GetMapping("/bookings")
    public ResponseEntity<?> getMyBookings(@AuthenticationPrincipal SecurityUser principal) {
        Long studentId = principal.getUser().getId();
        return ResponseEntity.ok(bookingRepo.findByStudentId(studentId));
    }

    /** 查詢自己的錢包交易紀錄 */
    @GetMapping("/wallet/logs")
    public ResponseEntity<?> getMyWalletLogs(@AuthenticationPrincipal SecurityUser principal) {
        Long userId = principal.getUser().getId();
        return ResponseEntity.ok(walletLogRepo.findByUserId(userId));
    }
}
