package com.learning.api.security;


import com.learning.api.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.exp-minutes}")
    private long expMinutes;

    private Key getSign() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 生成令牌
    public String generateToken(User user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expMinutes * 60 * 1000);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .setIssuedAt(now) // token 發放時間
                .setExpiration(exp) // token 過期時間
                .signWith(getSign())
                .compact();
    }

    // 解析令牌
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSign())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 提取 id
    public Long extractUserId(String token){
        Claims claims = parseToken(token);
        String subject = claims.getSubject();

        return Long.valueOf(subject);
    }

    // email
    public String extractUserEmail(String token){
        Claims claims = parseToken(token);
        return claims.get("email", String.class);
    }

    // role
    public String extractUserRole(String token){
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    // 時效
    public boolean isTokenExp(String token){
        Claims claims = parseToken(token);

        Long exp = claims.getExpiration().getTime();
        Long now = System.currentTimeMillis();

        if (exp>now) return false;
        return true;
    }
}
