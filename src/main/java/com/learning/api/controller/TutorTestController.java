package com.learning.api.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tutor")
public class TutorTestController {
    @GetMapping("/test")
    public String ok(){
        return "tutor test ok";
    }
}
