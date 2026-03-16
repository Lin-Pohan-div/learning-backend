package com.learning.api.service;

import com.learning.api.dto.auth.LoginReq;
import com.learning.api.dto.auth.LoginResp;
import com.learning.api.entity.User;
import com.learning.api.repo.MemberRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.learning.api.enums.UserRole;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepo memberRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_EMAIL = "authsvc@example.com";
    private static final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setName("Auth Service User");
        user.setEmail(TEST_EMAIL);
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        user.setRole(UserRole.STUDENT);
        user.setWallet(0L);
        memberRepo.save(user);
    }

    @Test
    void loginReq_validCredentials_returnsToken() {
        LoginReq req = new LoginReq();
        req.setEmail(TEST_EMAIL);
        req.setPassword(TEST_PASSWORD);

        LoginResp resp = authService.loginReq(req);

        assertNotNull(resp);
        assertNotNull(resp.getToken());
        assertFalse(resp.getToken().isEmpty());
    }

    @Test
    void loginReq_wrongPassword_throwsIllegalArgumentException() {
        LoginReq req = new LoginReq();
        req.setEmail(TEST_EMAIL);
        req.setPassword("wrongpassword");

        assertThrows(IllegalArgumentException.class, () -> authService.loginReq(req));
    }

    @Test
    void loginReq_nonExistentUser_throwsIllegalArgumentException() {
        LoginReq req = new LoginReq();
        req.setEmail("nonexistent@example.com");
        req.setPassword(TEST_PASSWORD);

        assertThrows(IllegalArgumentException.class, () -> authService.loginReq(req));
    }
}
