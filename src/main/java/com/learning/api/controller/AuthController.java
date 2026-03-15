package com.learning.api.controller;

import com.learning.api.dto.auth.*;
import com.learning.api.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173") // 部署好網域再換
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private AuthService authService;

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
