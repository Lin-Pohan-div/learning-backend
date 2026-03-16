package com.learning.api.service;

import com.learning.api.dto.auth.LoginReq;
import com.learning.api.dto.auth.LoginResp;
import com.learning.api.entity.User;
import com.learning.api.enums.UserRole;
import com.learning.api.repo.UserRepository;
import com.learning.api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User makeUser(Long id, String email, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("$2a$10$abcdefghijklmnopqrstuv");
        user.setRole(role);
        user.setWallet(0L);
        return user;
    }

    private LoginReq makeLoginReq(String email, String password) {
        LoginReq req = new LoginReq();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    @Test
    void loginReq_whenValidCredentials_returnsTokenOnly() {
        User user = makeUser(1L, "user@test.com", UserRole.STUDENT);
        LoginReq req = makeLoginReq("user@test.com", "password123");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        LoginResp resp = authService.loginReq(req);

        assertThat(resp.getToken()).isEqualTo("jwt-token");
        // AuthService only sets token, not user field
        assertThat(resp.getUser()).isNull();
    }

    @Test
    void loginReq_whenEmailNotFound_throwsIllegalArgument() {
        LoginReq req = makeLoginReq("notfound@test.com", "password123");
        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.loginReq(req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void loginReq_whenWrongPassword_throwsIllegalArgument() {
        User user = makeUser(1L, "user@test.com", UserRole.STUDENT);
        LoginReq req = makeLoginReq("user@test.com", "wrongpassword");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.loginReq(req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void loginReq_generatesTokenUsingJwtService() {
        User user = makeUser(1L, "user@test.com", UserRole.STUDENT);
        LoginReq req = makeLoginReq("user@test.com", "password123");

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("generated-token");

        LoginResp resp = authService.loginReq(req);

        verify(jwtService).generateToken(user);
        assertThat(resp.getToken()).isEqualTo("generated-token");
    }
}
