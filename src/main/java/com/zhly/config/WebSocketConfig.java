package com.zhly.config;

import com.zhly.websocket.UserHandshakeInterceptor;
import com.zhly.websocket.UserMessageWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置
 *
 * @author zhly
 * @since 2025-11-19
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private UserMessageWebSocketHandler userMessageWebSocketHandler;

    @Autowired
    private UserHandshakeInterceptor userHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(userMessageWebSocketHandler, "/api/ws/chat")
                .addInterceptors(userHandshakeInterceptor)
                .setAllowedOrigins("*")
                .setAllowedOriginPatterns("*");
    }
}

