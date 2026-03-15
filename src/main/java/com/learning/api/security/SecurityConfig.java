package com.learning.api.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(auth -> auth
                        // CORS 預檢
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 認證端點（登入 / 註冊）
                        .requestMatchers("/api/auth/**").permitAll()

                        // 公開資源（GET only）
                        .requestMatchers(HttpMethod.GET, "/api/teacher/**").permitAll()      // 家教公開個人資料
                        // 老師寫入操作需要 TEACHER 角色
                        .requestMatchers(HttpMethod.POST, "/api/teacher/**").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.PUT, "/api/teacher/**").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.DELETE, "/api/teacher/**").hasRole("TEACHER")
                        .requestMatchers("/api/reviews/**").permitAll()
                        .requestMatchers("/api/chat-messages/**").permitAll()
                        .requestMatchers("/api/lesson-feedbacks/**").permitAll()

                        // WebSocket (SockJS handshake + STOMP)
                        .requestMatchers("/ws/**").permitAll()

                        // 上傳檔案靜態資源
                        .requestMatchers("/uploads/**").permitAll()

                        // 靜態頁面 / 測試用
                        .requestMatchers("/*.html").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers("/test-email/**").hasRole("ADMIN")

                        // Swagger / Actuator（開發階段）
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/**").permitAll()

                        // 其餘請求需登入
                        .anyRequest().authenticated()
                )

                // JWT filter 在 Spring Security 的 UsernamePasswordAuthenticationFilter 之前執行
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 開發階段放寬，正式上線改為前端網址
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
