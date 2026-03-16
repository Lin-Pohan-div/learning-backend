package com.learning.api.controller;

import com.learning.api.dto.auth.*;
import com.learning.api.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterReq registerReq){
        memberService.register(registerReq);
        return ResponseEntity.ok().body(Map.of("msg", "註冊成功"));
    }

    @PostMapping("/login")
    public LoginResp login(@Valid @RequestBody LoginReq loginReq){
        return authService.loginReq(loginReq);
    }
}
