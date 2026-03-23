package com.learning.api.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // session
                // ->
                // token
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 授權設定
                .authorizeHttpRequests(auth -> auth
                        // 完全公開
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/view/**").permitAll()
                        .requestMatchers("/api/tutor/**").permitAll()  // 前台老師資料頁公開
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/teacher/schedules/*").permitAll()  // 查詢課表公開
                        // 老師後台專用
                        .requestMatchers("/api/teacher/**").hasRole("TUTOR")

                        // 登入才能預約
                        .requestMatchers("/api/bookings/**").authenticated()
                        // 登入才能結帳
                        .requestMatchers("/api/shop/**").authenticated()
                        // 學生專用
                        .requestMatchers("/api/student/**").hasRole("STUDENT")

                        // 管理者
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 其他都要登入
                        .anyRequest().authenticated()
                )
                // 檢查 token
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 加入全域 CORS 設定，讓所有端點都允許跨來源請求
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}