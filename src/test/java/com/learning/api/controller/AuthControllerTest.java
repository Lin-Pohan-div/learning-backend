package com.learning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.api.dto.auth.LoginReq;
import com.learning.api.dto.auth.LoginResp;
import com.learning.api.dto.auth.RegisterReq;
import com.learning.api.enums.UserRole;
import com.learning.api.exception.GlobalExceptionHandler;
import com.learning.api.service.AuthService;
import com.learning.api.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private MemberService memberService;
    @Mock private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules(); // 支援 LocalDate 序列化

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── 測試資料工廠 ──────────────────────────────────────────────────────────

    private RegisterReq makeRegisterReq(String email, String password) {
        RegisterReq req = new RegisterReq();
        req.setName("Test User");
        req.setEmail(email);
        req.setPassword(password);
        req.setBirthday(LocalDate.of(2000, 1, 1));
        req.setRole(UserRole.STUDENT);
        return req;
    }

    private LoginReq makeLoginReq(String email, String password) {
        LoginReq req = new LoginReq();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    // ── POST /api/auth/register ───────────────────────────────────────────────

    @Test
    void register_validRequest_returns200WithMsg() throws Exception {
        doNothing().when(memberService).register(any(RegisterReq.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeRegisterReq("new@test.com", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("註冊成功"));
    }

    @Test
    void register_duplicateEmail_returns400WithMsg() throws Exception {
        doThrow(new IllegalArgumentException("此 email 已被註冊"))
                .when(memberService).register(any(RegisterReq.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeRegisterReq("exist@test.com", "password123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg", containsString("email")));
    }

    // ── POST /api/auth/login ──────────────────────────────────────────────────

    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        LoginResp resp = new LoginResp("jwt-token-abc");
        when(authService.loginReq(any(LoginReq.class))).thenReturn(resp);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeLoginReq("user@test.com", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-abc"));
    }

    @Test
    void login_wrongPassword_returns400WithMsg() throws Exception {
        when(authService.loginReq(any(LoginReq.class)))
                .thenThrow(new IllegalArgumentException("帳號或密碼錯誤"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeLoginReq("user@test.com", "wrongpass1"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("帳號或密碼錯誤"));
    }

    @Test
    void login_emailNotFound_returns400WithMsg() throws Exception {
        when(authService.loginReq(any(LoginReq.class)))
                .thenThrow(new IllegalArgumentException("帳號或密碼錯誤"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeLoginReq("notfound@test.com", "password123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("帳號或密碼錯誤"));
    }

    @Test
    void login_returnsTokenFromAuthService() throws Exception {
        LoginResp resp = new LoginResp("generated-token");
        when(authService.loginReq(any(LoginReq.class))).thenReturn(resp);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeLoginReq("user@test.com", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("generated-token"));

        verify(authService).loginReq(any(LoginReq.class));
    }
}
