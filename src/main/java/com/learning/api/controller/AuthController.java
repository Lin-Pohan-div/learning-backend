package com.learning.api.controller;

import com.learning.api.dto.auth.*;
import com.learning.api.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterReq registerReq){
        try{
            memberService.register(registerReq);
            return ResponseEntity.ok().body(Map.of("msg", "註冊成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("msg", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginReq loginReq){
        try{
            memberService.login(loginReq);
            return ResponseEntity.ok().body(Map.of("msg", "登入成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("msg", e.getMessage()));
        }
    }
}
