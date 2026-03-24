package com.learning.api.controller;

import com.learning.api.dto.auth.UserResp;
import com.learning.api.entity.User;
import com.learning.api.security.SecurityUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/users")
public class MeController {

    @GetMapping("/me")
    public ResponseEntity<UserResp> getMe(@AuthenticationPrincipal SecurityUser securityUser) {
        User user = securityUser.getUser();
        UserResp resp = new UserResp(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBirthday(),
                user.getRole(),
                user.getWallet(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
        return ResponseEntity.ok(resp);
    }
}
