package com.learning.api.controller;

import com.learning.api.annotation.ApiController;
import com.learning.api.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@ApiController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * 儲值 — 任何登入用戶皆可使用。
     * Body: { "userId": 1, "amount": 500 }
     */
    @PostMapping("/topup")
    public ResponseEntity<?> topUp(@RequestBody Map<String, Long> body) {
        Long userId = body.get("userId");
        Long amount = body.get("amount");
        if (userId == null || amount == null) {
            return ResponseEntity.status(400).body(Map.of("msg", "userId 與 amount 為必填"));
        }
        String result = walletService.topUp(userId, amount);
        if (!"success".equals(result)) {
            return ResponseEntity.status(400).body(Map.of("msg", result));
        }
        return ResponseEntity.ok(Map.of("msg", "儲值成功"));
    }

    /**
     * 提領 — 教師專用，需有銀行帳戶資訊。
     * Body: { "tutorId": 1, "amount": 1000 }
     */
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody Map<String, Long> body) {
        Long tutorId = body.get("tutorId");
        Long amount = body.get("amount");
        if (tutorId == null || amount == null) {
            return ResponseEntity.status(400).body(Map.of("msg", "tutorId 與 amount 為必填"));
        }
        String result = walletService.withdraw(tutorId, amount);
        if (!"success".equals(result)) {
            return ResponseEntity.status(400).body(Map.of("msg", result));
        }
        return ResponseEntity.ok(Map.of("msg", "提領申請成功"));
    }

    /**
     * 查詢交易紀錄。
     */
    @GetMapping("/logs/{userId}")
    public ResponseEntity<?> getLogs(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getLogs(userId));
    }
}
