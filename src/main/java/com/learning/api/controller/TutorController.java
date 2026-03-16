package com.learning.api.controller;

import com.learning.api.dto.tutor.TutorReq;
import com.learning.api.entity.Tutor;
import com.learning.api.service.TutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/tutor")
public class TutorController {

    @Autowired
    private TutorService tutorService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getTutor(@PathVariable Long id) {
        Tutor tutor = tutorService.getTutor(id);
        if (tutor == null)
            return ResponseEntity.status(404).body(Map.of("msg", "查無老師資料"));
        return ResponseEntity.ok(tutor);
    }

    @PostMapping
    public ResponseEntity<?> createTutor(@RequestBody TutorReq req) {
        if (!tutorService.createTutor(req))
            return ResponseEntity.status(400).body(Map.of("msg", "建立失敗"));
        return ResponseEntity.ok(Map.of("msg", "ok"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTutor(@PathVariable Long id, @RequestBody TutorReq req) {
        if (!tutorService.updateTutor(id, req))
            return ResponseEntity.status(400).body(Map.of("msg", "更新失敗"));
        return ResponseEntity.ok(Map.of("msg", "ok"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTutor(@PathVariable Long id) {
        if (!tutorService.deleteTutor(id))
            return ResponseEntity.status(404).body(Map.of("msg", "查無老師資料"));
        return ResponseEntity.ok(Map.of("msg", "ok"));
    }
}
