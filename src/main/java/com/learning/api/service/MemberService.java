package com.learning.api.service;

import com.learning.api.dto.auth.*;
import com.learning.api.entity.*;
import com.learning.api.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    @Autowired
    private MemberRepo memberRepo;

    public void register(RegisterReq registerReq) {
        String email = registerReq.getEmail().trim().toLowerCase();

        // check email
        if (memberRepo.existsByEmail(email)) throw new IllegalArgumentException("此 email 已被註冊");

        // password
        String rawPassword = registerReq.getPassword();
        String password = rawPassword.trim();
        String hashPassword = BCrypt.hashpw(password, BCrypt.gensalt());

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
    public void login(LoginReq loginReq) {
        String rawEmail = loginReq.getEmail().trim().toLowerCase();
        String rawPassword = loginReq.getPassword().trim();

        User user = memberRepo.findByEmail(rawEmail).orElse(null);

        if (user == null) throw new IllegalArgumentException("你沒有註冊喔");

        if (!BCrypt.checkpw(rawPassword, user.getPassword())) throw new IllegalArgumentException("密碼錯誤");

    }
}
