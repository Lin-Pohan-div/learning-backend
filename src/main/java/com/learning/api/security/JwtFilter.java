package com.learning.api.security;

import com.learning.api.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@org.springframework.context.annotation.Profile("!test")
public class JwtFilter extends OncePerRequestFilter {


    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 檢查標頭
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        // 去掉 Bearer
        String token = authHeader.substring(7);

        String email = jwtService.email(token);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
        SecurityUser securityUser = (SecurityUser) userDetails;
        User user = securityUser.getUser();

        if (jwtService.isTokenValid(token, user)){
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, // who
                            null, // 密碼先不比
                            userDetails.getAuthorities() // role
                    );
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        System.out.printf("authHeader = %s", authHeader);
        System.out.printf("token = %s", token) ;
        System.out.printf("email = %s", email);
        System.out.printf("isTokenValid = %s", jwtService.isTokenValid(token, user));

        filterChain.doFilter(request, response);
    }
}
