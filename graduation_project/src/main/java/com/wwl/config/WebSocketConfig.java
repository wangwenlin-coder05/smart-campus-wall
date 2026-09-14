package com.wwl.config;

import com.wwl.websocket.handler.ChatWebSocketHandler;
import com.wwl.websocket.handler.DisputeCountWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;

    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 聊天 WebSocket：前端通过 /ws/chat 建立长连接并实时收发消息。
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOriginPatterns("*");

        // 纠纷数量推送 WebSocket：保留项目已有功能。
        registry.addHandler(new DisputeCountWebSocketHandler(), "/ws/dispute")
                .setAllowedOriginPatterns("*");
    }
}
