package com.learning.api.config;

import com.learning.api.repo.UserRepo;
import com.learning.api.security.CustomUserDetailsService;
import com.learning.api.security.JwtService;
import com.learning.api.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * STOMP 連線 JWT 認證攔截器
 * 僅在 CONNECT frame 時驗證 Authorization: Bearer {token}
 * 驗證成功後設定 Principal，讓後續 MessageMapping 可取得使用者資訊
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepo userRepo;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 非 CONNECT frame，直接放行（Session 已在 CONNECT 時驗證過）
            return message;
        }

        String header = accessor.getFirstNativeHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            log.warn("[WebSocketAuth] 缺少 Authorization header，拒絕連線");
            throw new MessageDeliveryException("WebSocket 連線需要 Authorization: Bearer {token}");
        }

        String token = header.substring(7);

        try {
            String email = jwtService.email(token);

            userRepo.findByEmail(email).orElseThrow(
                    () -> new MessageDeliveryException("使用者不存在: " + email)
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!jwtService.isTokenValid(token, ((SecurityUser) userDetails).getUser())) {
                throw new MessageDeliveryException("JWT token 已過期或無效");
            }

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            accessor.setUser(auth);
            log.info("[WebSocketAuth] 認證成功 email={}", email);

        } catch (MessageDeliveryException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[WebSocketAuth] JWT 驗證失敗: {}", e.getMessage());
            throw new MessageDeliveryException("JWT 驗證失敗: " + e.getMessage());
        }

        return message;
    }
}
