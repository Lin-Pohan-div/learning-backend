package com.learning.api.service;

import com.learning.api.dto.auth.RegisterReq;
import com.learning.api.entity.User;
import com.learning.api.enums.UserRole;
import com.learning.api.repo.MemberRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepo memberRepo;

    private RegisterReq buildReq(String name, String email, String password, UserRole role) {
        RegisterReq req = new RegisterReq();
        req.setName(name);
        req.setEmail(email);
        req.setPassword(password);
        req.setRole(role);
        req.setBirthday(LocalDate.of(1990, 1, 1));
        return req;
    }

    @Test
    void register_validRequest_savesUser() {
        RegisterReq req = buildReq("New Member", "newmember@example.com", "password123", UserRole.STUDENT);

        memberService.register(req);

        Optional<User> saved = memberRepo.findByEmail("newmember@example.com");
        assertTrue(saved.isPresent());
        assertEquals("New Member", saved.get().getName());
        assertEquals(UserRole.STUDENT, saved.get().getRole());
    }

    @Test
    void register_duplicateEmail_throwsIllegalArgumentException() {
        RegisterReq req = buildReq("First User", "duplicate@example.com", "password123", UserRole.STUDENT);
        memberService.register(req);

        RegisterReq req2 = buildReq("Second User", "duplicate@example.com", "password123", UserRole.STUDENT);
        assertThrows(IllegalArgumentException.class, () -> memberService.register(req2));
    }

    @Test
    void register_emailNormalized_savesAsLowercase() {
        RegisterReq req = buildReq("Upper Email", "UPPER@Example.COM", "password123", UserRole.STUDENT);

        memberService.register(req);

        Optional<User> saved = memberRepo.findByEmail("upper@example.com");
        assertTrue(saved.isPresent());
    }

    @Test
    void register_passwordIsHashed() {
        RegisterReq req = buildReq("Hash Test", "hashtest@example.com", "password123", UserRole.STUDENT);

        memberService.register(req);

        User saved = memberRepo.findByEmail("hashtest@example.com").orElseThrow();
        assertNotEquals("password123", saved.getPassword());
        assertTrue(saved.getPassword().startsWith("$2"));  // BCrypt hash prefix
    }
}
