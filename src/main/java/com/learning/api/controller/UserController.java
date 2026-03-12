package com.learning.api.controller;

import com.learning.api.annotation.ApiController;
import com.learning.api.entity.User;
import com.learning.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@ApiController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (!userService.register(user)) {
            return ResponseEntity.status(400).body(Map.of("message", "註冊失敗"));
        }
        return ResponseEntity.ok(Map.of("message", "歡迎"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User member) {
        if (!userService.login(member.getEmail(), member.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "帳號或密碼錯誤"));
        }
        return ResponseEntity.ok(Map.of("message", "歡迎"));
    }
}
