package com.learning.api.controller;

import com.learning.api.dto.CheckoutReq;
import com.learning.api.enums.UserRole;
import com.learning.api.security.SecurityUser;
import com.learning.api.service.CheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/shop")
@CrossOrigin(origins = "*")
public class CheckoutController {

    @Autowired private CheckoutService checkoutService;

    // @PostMapping("/purchase")//old
    // public ResponseEntity<?> purchase(@RequestBody CheckoutReq req) {
    //     String result = checkoutService.processPurchase(req);

    //     if ("success".equals(result)) {
    //         return ResponseEntity.ok(Map.of("msg", "購買並預約成功！"));
    //     } else if ("餘額不足".equals(result)) {
    //         return ResponseEntity.status(402).body(Map.of("msg", result, "action", "recharge"));
    //     } else {
    //         return ResponseEntity.badRequest().body(Map.of("msg", result));
    //     }
    // } 

    @PostMapping("/purchase")
    public ResponseEntity<?> purchase(@RequestBody CheckoutReq req, @AuthenticationPrincipal SecurityUser me ) {
        Long studentId = me.getUser().getId(); 
        UserRole role = me.getUser().getRole();
        String result = checkoutService.processPurchase(req, studentId,role);

        if ("success".equals(result)) {
            return ResponseEntity.ok(Map.of("msg", "購買並預約成功！"));
        } else if ("餘額不足".equals(result)) {
            return ResponseEntity.status(402).body(Map.of("msg", result, "action", "recharge"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("msg", result));
        }
    }

}