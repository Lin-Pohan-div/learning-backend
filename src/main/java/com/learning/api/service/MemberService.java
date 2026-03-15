package com.learning.api.service;

import com.learning.api.dto.auth.*;
import com.learning.api.entity.*;
import com.learning.api.repo.*;
import com.learning.api.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    @Autowired
    private MemberRepo memberRepo;

    @Autowired
    private JwtService jwtService;

    public void register(RegisterReq registerReq) {
        String email = registerReq.getEmail().trim().toLowerCase();

        // check email
        if (memberRepo.existsByEmail(email)) throw new IllegalArgumentException("此 email 已被註冊");

        // password
        String rawPassword = registerReq.getPassword();
        // String password = rawPassword.trim();
        String hashPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        User user = buildMember(registerReq, email, hashPassword);
        memberRepo.save(user);
    }

    public User buildMember(RegisterReq registerReq, String email, String hashPassword){
        User newMember = new User();
        newMember.setName(registerReq.getName());
        newMember.setEmail(email);
        newMember.setPassword(hashPassword);
        newMember.setBirthday(registerReq.getBirthday());
        newMember.setRole(registerReq.getRole());
        newMember.setWallet(0);
        return newMember;
    }

    // login
    public LoginResp login(LoginReq loginReq) {
        String rawEmail = loginReq.getEmail().trim().toLowerCase();
        //String rawPassword = loginReq.getPassword().trim();
        String rawPassword = loginReq.getPassword();

        User user = memberRepo.findByEmail(rawEmail).orElse(null);

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
    }
}
