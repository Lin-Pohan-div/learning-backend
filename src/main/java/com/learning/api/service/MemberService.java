package com.learning.api.service;

import com.learning.api.dto.auth.*;
import com.learning.api.dto.auth.LoginResp;
import com.learning.api.dto.auth.LoginReq;
import com.learning.api.entity.*;
import com.learning.api.repo.UserRepository;
import com.learning.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    @Autowired
    private UserRepo memberRepo;

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public void register(RegisterReq registerReq) {
        String email = registerReq.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) throw new IllegalArgumentException("此 email 已被註冊");

        // password
        String rawPassword = registerReq.getPassword();
        // String password = rawPassword.trim();
        String hashPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        User user = buildMember(registerReq, email, hashPassword);
        userRepository.save(user);
    }

    public LoginResp login(LoginReq loginReq) {
        String email    = loginReq.getEmail().trim().toLowerCase();
        String password = loginReq.getPassword().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("帳號或密碼錯誤"));

        if (!BCrypt.checkpw(password, user.getPassword()))
            throw new IllegalArgumentException("帳號或密碼錯誤");

        String token = jwtService.generateToken(user);

        UserResp userResp = new UserResp(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBirthday(),
                user.getRole(),
                user.getWallet().intValue(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

        LoginResp resp = new LoginResp();
        resp.setToken(token);
        resp.setUser(userResp);
        return resp;
    }

    public User buildMember(RegisterReq registerReq, String email, String hashPassword) {
        User newMember = new User();
        newMember.setName(registerReq.getName());
        newMember.setEmail(email);
        newMember.setPassword(hashPassword);
        newMember.setBirthday(registerReq.getBirthday());
        newMember.setRole(registerReq.getRole());
        newMember.setWallet(0L);
        return newMember;
    }

    // login
/*     public LoginResp login(LoginReq loginReq) {
        String rawEmail = loginReq.getEmail().trim().toLowerCase();
        //String rawPassword = loginReq.getPassword().trim();
        String rawPassword = loginReq.getPassword();

        User user = userRepository.findByEmail(rawEmail).orElse(null);

        if (user == null) throw new IllegalArgumentException("你沒有註冊喔");

        if (!BCrypt.checkpw(rawPassword, user.getPassword())) throw new IllegalArgumentException("密碼錯誤");

        // token JwtService
        String token = jwtService.generateToken(user);

        UserResp userResp = new UserResp(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBirthday(),
                user.getRole(),
                user.getWallet(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );


        return new LoginResp(token);
    } */
}
