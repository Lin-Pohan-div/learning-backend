package com.learning.api.service;

<<<<<<< HEAD
import com.learning.api.entity.User;
import com.learning.api.repo.UserRepo;
=======
<<<<<<<< HEAD:src/main/java/com/learning/api/service/MemberService.java
import com.learning.api.entity.User;
import com.learning.api.repo.MemberRepo;
========
import com.learning.api.entity.*;
import com.learning.api.repo.*;
>>>>>>>> 82bba7905f5bf54ed159ef8cef8f85569dbfe782:src/main/java/com/learning/api/service/UserService.java
>>>>>>> 82bba7905f5bf54ed159ef8cef8f85569dbfe782
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

<<<<<<< HEAD
    // 註冊邏輯
    public boolean register(User user) {
        // 1. 檢查必填欄位 (這次多檢查了 Name)
        if (user == null || user.getEmail() == null || user.getPassword() == null || user.getName() == null) {
            System.out.println("❌ 註冊失敗：資料不完整 (姓名、信箱、密碼必填)");
=======
    // check email - findByEmail ( checkEmail != null return email )
    public boolean checkEmail (String email){
        // check null
        if(email==null||email.isEmpty()){
            return false;
        }

        // check rule
        if(!email.contains("@")){
            return false;
        }

        return userRepo.existsByEmail(email);
    }

    // if repo == null -> register
<<<<<<<< HEAD:src/main/java/com/learning/api/service/MemberService.java
    public boolean register(User member){
========
    public boolean register(User user){
>>>>>>>> 82bba7905f5bf54ed159ef8cef8f85569dbfe782:src/main/java/com/learning/api/service/UserService.java
        // check null
        if (user == null || user.getEmail() == null || user.getPassword() == null) {
>>>>>>> 82bba7905f5bf54ed159ef8cef8f85569dbfe782
            return false;
        }

        String email = user.getEmail().trim().toLowerCase();

<<<<<<< HEAD
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
        // 管理員身份由 role 判斷（role=0 為管理員，role=1 為學生，role=2 為老師）

        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));

        // 5. 存入資料庫
        userRepo.save(user);
        System.out.println("✅ 註冊成功！新使用者已存入 users 表。");
=======
        //check rule
        if (!(email.contains("@")) || email.isEmpty() || userRepo.existsByEmail(email)) {
            return false;
        }

        user.setEmail(email);

        // password
        String password = user.getPassword();

        // check null
        if(password == null){
            return false;
        }

        password = password.trim();

        // check rule
        if (password.length()<6){
            return false;
        }

        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));

        userRepo.save(user);
>>>>>>> 82bba7905f5bf54ed159ef8cef8f85569dbfe782

        return true;
    }

<<<<<<< HEAD
    // 登入邏輯
    public boolean login(String email, String password) {
=======
    // after register -> login
<<<<<<<< HEAD:src/main/java/com/learning/api/service/MemberService.java
    public boolean login(User member){
        if (member == null) return false;
========
    public boolean login(User user){
        if (user == null) return false;
>>>>>>>> 82bba7905f5bf54ed159ef8cef8f85569dbfe782:src/main/java/com/learning/api/service/UserService.java

        return true;
    }

    public boolean login(String email, String password){
>>>>>>> 82bba7905f5bf54ed159ef8cef8f85569dbfe782
        if (email == null || password == null) return false;

        String rawEmail = email.toLowerCase().trim();
        String rawPassword = password.trim();

<<<<<<< HEAD
        // 用 Email 去找人
        User user = userRepo.findByEmail(rawEmail).orElse(null);

        // 如果找不到人，或者密碼比對失敗，就回傳 false
        if (user == null) return false;
        return BCrypt.checkpw(rawPassword, user.getPassword());
    }
}
=======
<<<<<<<< HEAD:src/main/java/com/learning/api/service/MemberService.java
        User member = memberRepo.findByEmail(rawEmail).orElse(null);
========
        User user = userRepo.findByEmail(rawEmail).orElse(null);
>>>>>>>> 82bba7905f5bf54ed159ef8cef8f85569dbfe782:src/main/java/com/learning/api/service/UserService.java

        if (user == null) return false;

        return BCrypt.checkpw(rawPassword, user.getPassword());
    }

}
>>>>>>> 82bba7905f5bf54ed159ef8cef8f85569dbfe782
