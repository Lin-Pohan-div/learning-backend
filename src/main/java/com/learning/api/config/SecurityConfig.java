package com.learning.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // 取消預設
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // /api/auth
                .authorizeHttpRequests(auth -> auth
                        // 測試 正式上線要刪
                        // .requestMatchers("/api/TestController").permitAll()
                	    .requestMatchers("/test-email/**").permitAll()

                        //.requestMatchers("/api/auth/**").permitAll()
                        // teacher
                        //.requestMatchers("/api/teacher/**").hasRole("TEACHER")
                        // student
                        //.requestMatchers("/api/student/**").hasRole("STUDENT")

                        //chat-messages
                        .requestMatchers("/api/chat-messages/**").permitAll()
                        //reviews
                        .requestMatchers("/api/reviews/**").permitAll()
                        //lesson-feedbacks
                        .requestMatchers("/api/lesson-feedbacks/**").permitAll()
                        // WebSocket (SockJS handshake + STOMP)
                        .requestMatchers("/ws/**").permitAll()
                        // 測試用靜態頁面
                        .requestMatchers("/*.html").permitAll()
                        // 靜態資源
                        .requestMatchers("/favicon.ico").permitAll()
                        // teacher profile (public)
                        .requestMatchers("/api/teacher/**").permitAll()
                        
                        
                        .anyRequest().authenticated()
                );

        return httpSecurity.build();
    }
}
