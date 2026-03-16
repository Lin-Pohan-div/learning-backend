//package com.learning.api.service;
//
//import com.learning.api.entity.*;
//import com.learning.api.repo.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.bcrypt.BCrypt;
//import org.springframework.stereotype.Service;
//
//@Service
//public class UserService {
//
//    @Autowired
//    private UserRepo userRepo;
//
//    // check email - findByEmail ( checkEmail != null return email )
//    public boolean checkEmail (String email){
//        // check null
//        if(email==null||email.isEmpty()){
//            return false;
//        }
//
//        // check rule
//        if(!email.contains("@")){
//            return false;
//        }
//
//        return userRepo.existsByEmail(email);
//    }
//
//    // if repo == null -> register
//    public boolean register(User user){
//        // check null
//        if (user == null || user.getEmail() == null || user.getPassword() == null) {
//            return false;
//        }
//
//        String email = user.getEmail().trim().toLowerCase();
//
//        //check rule
//        if (!(email.contains("@")) || email.isEmpty() || userRepo.existsByEmail(email)) {
//            return false;
//        }
//
//        user.setEmail(email);
//
//        // password
//        String password = user.getPassword();
//
//        // check null
//        if(password == null){
//            return false;
//        }
//
//        password = password.trim();
//
//        // check rule
//        if (password.length()<6){
//            return false;
//        }
//
//        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
//
//        userRepo.save(user);
//
//        return true;
//    }
//
//    // after register -> login
//    public boolean login(User user){
//        if (user == null) return false;
//
//        return true;
//    }
//
//    public boolean login(String email, String password){
//        if (email == null || password == null) return false;
//
//        String rawEmail = email.toLowerCase().trim();
//        String rawPassword = password.trim();
//
//        User user = userRepo.findByEmail(rawEmail).orElse(null);
//
//        if (user == null) return false;
//
//        return BCrypt.checkpw(rawPassword, user.getPassword());
//    }
//
//}
