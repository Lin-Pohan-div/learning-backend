package com.learning.api.security;


import com.learning.api.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.exp-minutes}")
    private long expMinutes;

    private SecretKey getSignKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 生成 token
    public String generateToken(User user) {

        Date now = new Date();
        Date exp = new Date(now.getTime() + expMinutes * 60 * 1000);

        return Jwts.builder()
                // who
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())

                // token 發放 / 過期時間
                .issuedAt(now)
                .expiration(exp)
                .signWith(getSignKey())
                .compact();
    }

    // Payload
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 對 email
    public String email(String token) {
        return parseToken(token).getSubject();
    }
    public Long userId(String token){
        return parseToken(token).get("userId", Long.class);
    }
    // role
    public String role(String token){
        return parseToken(token).get("role", String.class);
    }

    // exp>now
    public boolean isTokenExp(String token) {
        Date isExp = parseToken(token).getExpiration();
        Date now = new Date();
        return isExp.before(now);
    }

    public boolean isTokenValid(String token, User user) {
        String email = email(token);
        return email.equals(user.getEmail()) && !isTokenExp(token);
    }
}