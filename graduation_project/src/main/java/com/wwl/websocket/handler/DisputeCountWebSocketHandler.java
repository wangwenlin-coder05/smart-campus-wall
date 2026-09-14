package com.wwl.websocket.handler;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DisputeCountWebSocketHandler extends TextWebSocketHandler {

    // 保存每个骑手的 WebSocket 连接，key 是骑手 userId
    private static final Map<String, WebSocketSession> riderSessions = new ConcurrentHashMap<>();

    // 连接建立时触发
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String riderUserId = getRiderIdFromSession(session);
        if (riderUserId != null) {
            riderSessions.put(riderUserId, session);
            System.out.println("骑手 " + riderUserId + " 已连接 WebSocket");
        }
    }

    // 收到消息时触发（这里不需要处理，前端可以不发消息）
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    }

    // 连接关闭时移除
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String riderUserId = getRiderIdFromSession(session);
        if (riderUserId != null) {
            riderSessions.remove(riderUserId);
            System.out.println("骑手 " + riderUserId + " 断开 WebSocket");
        }
    }

    // 给指定骑手推送纠纷数量
    public static void pushDisputeCountToRider(String riderUserId, int count) {
        if (riderUserId == null || riderUserId.trim().isEmpty()) {
            System.out.println("警告：无法推送纠纷数量，骑手用户ID为空");
            return;
        }
        WebSocketSession session = riderSessions.get(riderUserId);
        if (session != null && session.isOpen()) {
            try {
                String json = "{\"type\":\"dispute_count\",\"count\":" + count + "}";
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 从 URL 参数中提取骑手 ID（前端连接时需带 riderUserId 参数）
    private String getRiderIdFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();  // 例如 "riderUserId=202605271111"
        if (query != null && query.contains("riderUserId=")) {
            return query.split("riderUserId=")[1].split("&")[0];
        }
        return null;
    }
}