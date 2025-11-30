package com.zhly.websocket;

import com.zhly.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket握手拦截器，负责校验token并获取用户ID
 *
 * @author zhly
 * @since 2025-11-19
 */
@Component
public class UserHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(UserHandshakeInterceptor.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        log.info("WebSocket握手请求: {}", request.getURI());
        
        String token = null;

        // 1. 尝试从Header中获取Authorization
        var headers = request.getHeaders();
        if (headers.containsKey("Authorization")) {
            token = headers.getFirst("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            log.debug("从Header获取token: {}", token != null ? "已获取" : "未获取");
        }

        // 2. 尝试从Query参数获取token
        if (!StringUtils.hasText(token) && request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
            token = httpServletRequest.getParameter("token");
            log.debug("从Query参数获取token: {}", token != null ? "已获取" : "未获取");
        }

        if (!StringUtils.hasText(token)) {
            log.warn("WebSocket握手失败: 未找到token");
            return false;
        }

        try {
            if (!jwtUtil.validateToken(token)) {
                log.warn("WebSocket握手失败: Token验证失败");
                return false;
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                log.warn("WebSocket握手失败: 无法从Token获取用户ID");
                return false;
            }

            attributes.put("userId", userId);
            log.info("WebSocket握手成功: 用户ID={}", userId);
            return true;
        } catch (Exception e) {
            log.error("WebSocket握手异常: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}

