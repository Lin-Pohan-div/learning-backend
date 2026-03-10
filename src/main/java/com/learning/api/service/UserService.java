package com.learning.api.service;

import com.learning.api.entity.User;
import com.learning.api.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    // 註冊邏輯
    public boolean register(User user) {
        // 1. 檢查必填欄位 (這次多檢查了 Name)
        if (user == null || user.getEmail() == null || user.getPassword() == null || user.getName() == null) {
            System.out.println("❌ 註冊失敗：資料不完整 (姓名、信箱、密碼必填)");
            return false;
        }

        String email = user.getEmail().trim().toLowerCase();

        // 2. 檢查 Email 格式與是否重複
        if (!email.contains("@") || email.isEmpty() || userRepo.existsByEmail(email)) {
            System.out.println("❌ 註冊失敗：信箱格式錯誤或已經被註冊過");
            return false;
        }
        user.setEmail(email);

        // 3. 檢查密碼長度
        String password = user.getPassword().trim();
        if (password.length() < 6) {
            System.out.println("❌ 註冊失敗：密碼長度不足 6 碼");
            return false;
        }

        // 4. 設定預設值與密碼加密
        if (user.getRole() == null) {
            user.setRole(1); // 如果前端沒傳，預設給 1 (學生)
        }
        if (user.getWallet() == null) {
            user.setWallet(0); // 預設錢包為 0
        }
        if (user.getIsAdmin() == null) {
            user.setIsAdmin((byte) 0); // 預設不是管理員
        }

        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));

        // 5. 存入資料庫
        userRepo.save(user);
        System.out.println("✅ 註冊成功！新使用者已存入 users 表。");

        return true;
    }

    // 登入邏輯
    public boolean login(String email, String password) {
        if (email == null || password == null) return false;

        String rawEmail = email.toLowerCase().trim();
        String rawPassword = password.trim();

        // 用 Email 去找人
        User user = userRepo.findByEmail(rawEmail).orElse(null);

        // 如果找不到人，或者密碼比對失敗，就回傳 false
        if (user == null) return false;
        return BCrypt.checkpw(rawPassword, user.getPassword());
    }
}