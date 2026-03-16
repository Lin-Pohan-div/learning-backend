package com.learning.api.service;

import com.learning.api.dto.auth.LoginReq;
import com.learning.api.dto.auth.LoginResp;
import com.learning.api.dto.auth.RegisterReq;
import com.learning.api.entity.User;
import com.learning.api.enums.UserRole;
import com.learning.api.repo.UserRepository;
import com.learning.api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private MemberService memberService;

    private RegisterReq makeRegisterReq(String email, String password, UserRole role) {
        RegisterReq req = new RegisterReq();
        req.setName("Test User");
        req.setEmail(email);
        req.setPassword(password);
        req.setRole(role);
        return req;
    }

    private LoginReq makeLoginReq(String email, String password) {
        LoginReq req = new LoginReq();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

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

    // ── register ─────────────────────────────────────────────────────────────

    @Test
    void register_whenNewEmail_savesUser() {
        RegisterReq req = makeRegisterReq("new@test.com", "password123", UserRole.STUDENT);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);

        memberService.register(req);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_whenDuplicateEmail_throwsIllegalArgument() {
        RegisterReq req = makeRegisterReq("exist@test.com", "password123", UserRole.STUDENT);
        when(userRepository.existsByEmail("exist@test.com")).thenReturn(true);

        assertThatThrownBy(() -> memberService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email");
    }

    @Test
    void register_emailIsNormalizedBeforeCheck() {
        RegisterReq req = makeRegisterReq("  UPPER@Test.COM  ", "password123", UserRole.STUDENT);
        when(userRepository.existsByEmail("upper@test.com")).thenReturn(false);

        memberService.register(req);

        verify(userRepository).existsByEmail("upper@test.com");
    }

    @Test
    void register_passwordIsHashed() {
        RegisterReq req = makeRegisterReq("new@test.com", "plainpassword", UserRole.STUDENT);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);

        memberService.register(req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getPassword()).isNotEqualTo("plainpassword");
        assertThat(BCrypt.checkpw("plainpassword", saved.getPassword())).isTrue();
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_whenValidCredentials_returnsLoginRespWithToken() {
        String raw = "password123";
        String hashed = BCrypt.hashpw(raw, BCrypt.gensalt());
        User user = makeUser(1L, "user@test.com", UserRole.STUDENT);
        user.setPassword(hashed);

        LoginReq req = makeLoginReq("user@test.com", raw);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        LoginResp resp = memberService.login(req);

        assertThat(resp.getToken()).isEqualTo("jwt-token");
        assertThat(resp.getUser()).isNotNull();
        assertThat(resp.getUser().getEmail()).isEqualTo("user@test.com");
    }

    @Test
    void login_whenEmailNotFound_throwsIllegalArgument() {
        LoginReq req = makeLoginReq("notfound@test.com", "password123");
        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.login(req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void login_whenWrongPassword_throwsIllegalArgument() {
        String hashed = BCrypt.hashpw("correctpassword", BCrypt.gensalt());
        User user = makeUser(1L, "user@test.com", UserRole.STUDENT);
        user.setPassword(hashed);

        LoginReq req = makeLoginReq("user@test.com", "wrongpassword");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> memberService.login(req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void login_emailIsTrimmedAndLowercased() {
        String raw = "password123";
        String hashed = BCrypt.hashpw(raw, BCrypt.gensalt());
        User user = makeUser(1L, "user@test.com", UserRole.STUDENT);
        user.setPassword(hashed);

        LoginReq req = makeLoginReq("  USER@TEST.COM  ", raw);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("token");

        memberService.login(req);

        verify(userRepository).findByEmail("user@test.com");
    }
}
