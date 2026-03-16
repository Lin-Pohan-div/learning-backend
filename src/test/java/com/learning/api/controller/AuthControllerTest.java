package com.learning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.learning.api.entity.User;
import com.learning.api.repo.MemberRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.learning.api.enums.UserRole;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class AuthControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MemberRepo memberRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private static final String TEST_EMAIL = "authtest@example.com";
    private static final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 建立一個已存在的使用者，供登入測試使用
        User user = new User();
        user.setName("Auth Test User");
        user.setEmail(TEST_EMAIL);
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        user.setRole(UserRole.STUDENT);
        user.setWallet(0L);
        memberRepo.save(user);
    }

    // ===================== POST /api/auth/register =====================

    @Test
    void register_validRequest_shouldReturn200() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "New User",
                "email", "newuser@example.com",
                "password", "password123",
                "role", 1
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("註冊成功"));
    }

    @Test
    void register_duplicateEmail_shouldReturn400() throws Exception {
        // TEST_EMAIL 已在 setUp 中建立
        Map<String, Object> body = Map.of(
                "name", "Duplicate User",
                "email", TEST_EMAIL,
                "password", "password123",
                "role", 1
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("此 email 已被註冊"));
    }

    @Test
    void register_blankName_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "   ",
                "email", "blankname@example.com",
                "password", "password123",
                "role", 1
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shortPassword_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Short Pass",
                "email", "shortpass@example.com",
                "password", "1234567",  // 7 chars, min is 8
                "role", 1
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_invalidEmail_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Invalid Email",
                "email", "not-an-email",
                "password", "password123",
                "role", 1
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ===================== POST /api/auth/login =====================

    @Test
    void login_validCredentials_shouldReturn200WithToken() throws Exception {
        Map<String, Object> body = Map.of(
                "email", TEST_EMAIL,
                "password", TEST_PASSWORD
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_wrongPassword_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "email", TEST_EMAIL,
                "password", "wrongpass123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("帳號或密碼錯誤"));
    }

    @Test
    void login_nonExistentUser_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of(
                "email", "notexist@example.com",
                "password", "password123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("帳號或密碼錯誤"));
    }
}
