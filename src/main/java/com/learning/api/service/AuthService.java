package com.learning.api.service;

import com.learning.api.dto.auth.LoginReq;
import com.learning.api.dto.auth.LoginResp;
import com.learning.api.entity.User;
import com.learning.api.repo.UserRepository;
import com.learning.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResp loginReq(LoginReq loginReq){
        // 查人
        User user = userRepository.findByEmail(loginReq.getEmail()).orElse(null);
        if (user == null) throw new IllegalArgumentException("帳號或密碼錯誤");

        // 查密碼
        boolean isU = passwordEncoder.matches(loginReq.getPassword(), user.getPassword());
        if (!isU) {
            throw new IllegalArgumentException("帳號或密碼錯誤");
        }

        String token = jwtService.generateToken(user);

        return new LoginResp(token);
    }
}
