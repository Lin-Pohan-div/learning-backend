//package com.learning.api.controller;
//
//import com.learning.api.entity.*;
//import com.learning.api.service.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@CrossOrigin(origins = "*")
//@RestController
//@RequestMapping("/api/auth")
//public class UserController {
//
//    @Autowired
//    private UserService userService;
//
//    @PostMapping("/register")
//    public ResponseEntity<?> register(@RequestBody User user){
//
//        if (!userService.register(user)) {
//            return ResponseEntity.status(400).body(Map.of("msg", "註冊失敗"));
//        }
//
//        return ResponseEntity.ok(Map.of("msg", "歡迎"));
//    }
//
//
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody User user){
//
//        if (!userService.login(user)){
//            return ResponseEntity.status(401).body(Map.of("msg", "帳號或密碼錯誤"));
//        }
//
//        return ResponseEntity.ok(Map.of("msg", "歡迎"));
//    }
//}
