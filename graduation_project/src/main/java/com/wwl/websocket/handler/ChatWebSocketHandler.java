package com.wwl.websocket.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wwl.model.entity.ChatMessage;
import com.wwl.service.ChatMessageService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天 WebSocket 处理器。
 *
 * 前端连接示例：
 * ws://localhost:8080/ws/chat?conversationId=chat_001&userId=1001&nickname=小王
 *
 * 说明：
 * 1. conversationId 用来区分不同会话，相同 conversationId 的用户会进入同一个聊天室。
 * 2. userId 用来区分发送人，前端收到自己发出的消息时可以显示在右侧。
 * 3. 收到聊天内容后先保存到数据库，再广播给当前会话在线用户。
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatMessageService chatMessageService;

    public ChatWebSocketHandler(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    /**
     * 按会话保存在线连接。
     * key: conversationId
     * value: 该会话下所有在线用户的 WebSocket session
     */
    private final Map<String, Map<String, WebSocketSession>> conversationSessions = new ConcurrentHashMap<>();

    private final Map<String, Object> sessionLocks = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String conversationId = getQueryValue(session, "conversationId", "public");
        String userId = getQueryValue(session, "userId", session.getId());
        String nickname = getQueryValue(session, "nickname", "匿名用户");
        String avatar = getQueryValue(session, "avatar", "");
        String sourceType = getQueryValue(session, "sourceType", "chat");
        String sourceId = getQueryValue(session, "sourceId", conversationId);
        String sourceTitle = getQueryValue(session, "sourceTitle", "");
        String sourceDesc = getQueryValue(session, "sourceDesc", "");
        String targetUrl = getQueryValue(session, "targetUrl", "");
        String title = getQueryValue(session, "title", "消息对话");
        String conversationAvatar = getQueryValue(session, "conversationAvatar", "");

        // 把常用信息放进 session，后续收到消息或断开连接时可直接读取。
        session.getAttributes().put("conversationId", conversationId);
        session.getAttributes().put("userId", userId);
        session.getAttributes().put("nickname", nickname);
        session.getAttributes().put("avatar", avatar);
        session.getAttributes().put("sourceType", sourceType);
        session.getAttributes().put("sourceId", sourceId);
        session.getAttributes().put("sourceTitle", sourceTitle);
        session.getAttributes().put("sourceDesc", sourceDesc);
        session.getAttributes().put("targetUrl", targetUrl);
        session.getAttributes().put("title", title);
        session.getAttributes().put("conversationAvatar", conversationAvatar);

        Map<String, WebSocketSession> sessions = conversationSessions
                .computeIfAbsent(conversationId, key -> new ConcurrentHashMap<>());
        removePreviousUserSessions(sessions, userId, session.getId());
        sessions.put(session.getId(), session);

        sendSystemMessage(conversationId, nickname + " 已进入聊天");
        sendOnlineCount(conversationId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        ChatClientMessage clientMessage = parseClientMessage(message.getPayload());
        String content = clientMessage.content() == null ? "" : clientMessage.content().trim();

        // 空消息不广播，避免前端误触产生空白气泡。
        if (content.isEmpty()) {
            return;
        }

        String conversationId = getSessionValue(session, "conversationId", "public");
        String userId = getSessionValue(session, "userId", session.getId());
        String nickname = getSessionValue(session, "nickname", "匿名用户");
        String avatar = getSessionValue(session, "avatar", "");
        String sourceType = getSessionValue(session, "sourceType", "chat");
        String sourceId = getSessionValue(session, "sourceId", conversationId);
        String sourceTitle = getSessionValue(session, "sourceTitle", "");
        String sourceDesc = getSessionValue(session, "sourceDesc", "");
        String targetUrl = getSessionValue(session, "targetUrl", "");
        String title = getSessionValue(session, "title", "消息对话");
        String conversationAvatar = getSessionValue(session, "conversationAvatar", "");

        ChatMessage savedMessage = new ChatMessage();
        savedMessage.setConversationId(conversationId);
        savedMessage.setSourceType(sourceType);
        savedMessage.setSourceId(sourceId);
        savedMessage.setSourceTitle(sourceTitle);
        savedMessage.setSourceDesc(sourceDesc);
        savedMessage.setTargetUrl(targetUrl);
        savedMessage.setConversationTitle(title);
        savedMessage.setConversationAvatar(conversationAvatar);
        savedMessage.setSenderId(userId);
        savedMessage.setSenderName(nickname);
        savedMessage.setSenderAvatar(avatar);
        savedMessage.setContent(content);
        savedMessage.setMessageType("chat");
        chatMessageService.saveMessage(savedMessage);

        ChatServerMessage serverMessage = new ChatServerMessage(
                savedMessage.getId(),
                "chat",
                conversationId,
                userId,
                nickname,
                avatar,
                content,
                savedMessage.getCreateTime().format(TIME_FORMATTER),
                savedMessage.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
                getOnlineCount(conversationId),
                title,
                sourceType,
                sourceId,
                sourceTitle,
                sourceDesc,
                targetUrl
        );

        broadcast(conversationId, serverMessage);
        pushChatNoticeToOtherUsers(savedMessage);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
        String conversationId = getSessionValue(session, "conversationId", "public");
        String nickname = getSessionValue(session, "nickname", "匿名用户");

        Map<String, WebSocketSession> sessions = conversationSessions.get(conversationId);
        if (sessions != null) {
            sessions.remove(session.getId());
            if (sessions.isEmpty()) {
                conversationSessions.remove(conversationId);
            }
        }

        sendSystemMessage(conversationId, nickname + " 已离开聊天");
        sendOnlineCount(conversationId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // 发生网络错误时主动关闭连接，afterConnectionClosed 会负责清理在线列表。
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    private ChatClientMessage parseClientMessage(String payload) {
        try {
            return objectMapper.readValue(payload, ChatClientMessage.class);
        } catch (JsonProcessingException e) {
            // 兼容前端直接发送纯文本的情况。
            return new ChatClientMessage(payload);
        }
    }

    private void sendSystemMessage(String conversationId, String content) throws IOException {
        // 系统通知类会话不发送进入/离开提示
        if (conversationId != null && conversationId.startsWith("notice_")) {
            return;
        }
        ChatServerMessage message = new ChatServerMessage(
                null,
                "system",
                conversationId,
                "system",
                "系统",
                "",
                content,
                LocalDateTime.now().format(TIME_FORMATTER),
                System.currentTimeMillis(),
                getOnlineCount(conversationId),
                null,
                null,
                null,
                null,
                null,
                null
        );
        broadcast(conversationId, message);
    }

    private void sendOnlineCount(String conversationId) throws IOException {
        ChatServerMessage message = new ChatServerMessage(
                null,
                "online",
                conversationId,
                "system",
                "系统",
                "",
                "",
                LocalDateTime.now().format(TIME_FORMATTER),
                System.currentTimeMillis(),
                getOnlineCount(conversationId),
                null,
                null,
                null,
                null,
                null,
                null
        );
        broadcast(conversationId, message);
    }

    private void broadcast(String conversationId, ChatServerMessage message) throws IOException {
        Map<String, WebSocketSession> sessions = conversationSessions.get(conversationId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        String payload = objectMapper.writeValueAsString(message);
        List<String> closedSessionIds = new ArrayList<>();

        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                Object lock = sessionLocks.computeIfAbsent(session.getId(), k -> new Object());
                    synchronized (lock) {
                       session.sendMessage(new TextMessage(payload));
                   }
            } else {
                closedSessionIds.add(session.getId());
            }
        }

        // 顺手清理已经关闭但还留在 Map 里的连接。
        closedSessionIds.forEach(sessions::remove);
    }

    private int getOnlineCount(String conversationId) {
        Map<String, WebSocketSession> sessions = conversationSessions.get(conversationId);
        if (sessions == null) {
            return 0;
        }
        Set<String> onlineUsers = new HashSet<>();
        for (WebSocketSession session : sessions.values()) {
            if (!session.isOpen()) {
                continue;
            }
            String userId = getSessionValue(session, "userId", session.getId());
            if (userId != null && !userId.isBlank()) {
                onlineUsers.add(userId);
            }
        }
        return onlineUsers.size();
    }

    private void removePreviousUserSessions(Map<String, WebSocketSession> sessions, String userId, String currentSessionId) {
        if (sessions == null || userId == null || userId.isBlank()) {
            return;
        }
        List<String> duplicateSessionIds = new ArrayList<>();
        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            WebSocketSession oldSession = entry.getValue();
            String oldUserId = getSessionValue(oldSession, "userId", "");
            if (!userId.equals(oldUserId) || entry.getKey().equals(currentSessionId)) {
                continue;
            }
            duplicateSessionIds.add(entry.getKey());
            try {
                if (oldSession.isOpen()) {
                    oldSession.close(CloseStatus.NORMAL);
                }
            } catch (IOException ignored) {
            }
        }
        duplicateSessionIds.forEach(sessions::remove);
    }

    private void pushChatNoticeToOtherUsers(ChatMessage message) {
        for (String receiverId : resolveReceiverIds(message.getConversationId(), message.getSenderId())) {
            ChatServerMessage notice = new ChatServerMessage(
                    message.getId(),
                    "chat_notice",
                    message.getConversationId(),
                    message.getSenderId(),
                    message.getSenderName(),
                    message.getSenderAvatar(),
                    message.getContent(),
                    message.getCreateTime().format(TIME_FORMATTER),
                    message.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    getOnlineCount(message.getConversationId()),
                    message.getSenderName(),
                    message.getSourceType(),
                    message.getSourceId(),
                    message.getSourceTitle() != null ? message.getSourceTitle() : "",
                    message.getSourceDesc() != null ? message.getSourceDesc() : "",
                    message.getTargetUrl() != null ? message.getTargetUrl() : ""
            );
            try {
                broadcast("notice_" + receiverId, notice);
            } catch (IOException ignored) {
            }
        }
    }

    private List<String> resolveReceiverIds(String conversationId, String senderId) {
        List<String> receivers = new ArrayList<>();
        if (conversationId == null || senderId == null || !conversationId.startsWith("friend_")) {
            return receivers;
        }

        String[] parts = conversationId.substring("friend_".length()).split("_");
        for (String part : parts) {
            if (!part.isBlank() && !part.equals(senderId) && !receivers.contains(part)) {
                receivers.add(part);
            }
        }
        return receivers;
    }

    private String getSessionValue(WebSocketSession session, String key, String defaultValue) {
        Object value = session.getAttributes().get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }

    private String getQueryValue(WebSocketSession session, String key, String defaultValue) {
        URI uri = session.getUri();
        if (uri == null) {
            return defaultValue;
        }
        String value = Objects.requireNonNullElse(
                UriComponentsBuilder.fromUri(uri).build().getQueryParams().getFirst(key),
                defaultValue
        );
        // 前端 URL 参数会被 encodeURIComponent 编码，这里解码后再入库，避免数据库出现 %E6%...。
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    /**
     * 前端发来的消息体，只需要 content 字段即可。
     */
    private record ChatClientMessage(String content) {
    }

    /**
     * 后端广播给前端的统一消息格式。
     */
    private record ChatServerMessage(
            Long id,
            String type,
            String conversationId,
            String senderId,
            String senderName,
            String senderAvatar,
            String content,
            String time,
            long timestamp,
            int onlineCount,
            String title,
            String sourceType,
            String sourceId,
            String sourceTitle,
            String sourceDesc,
            String targetUrl
    ) {
    }

    public void pushSystemNotice(ChatMessage message) {
        if (message == null || message.getConversationId() == null || message.getConversationId().isBlank()) {
            return;
        }

        ChatServerMessage serverMessage = new ChatServerMessage(
                message.getId(),
                message.getMessageType() == null ? "system" : message.getMessageType(),
                message.getConversationId(),
                message.getSenderId() == null ? "system" : message.getSenderId(),
                message.getSenderName() == null ? "系统通知" : message.getSenderName(),
                message.getSenderAvatar() == null ? "" : message.getSenderAvatar(),
                message.getContent() == null ? "" : message.getContent(),
                LocalDateTime.now().format(TIME_FORMATTER),
                System.currentTimeMillis(),
                getOnlineCount(message.getConversationId()),
                message.getConversationTitle(),
                message.getSourceType(),
                message.getSourceId(),
                message.getSourceTitle() != null ? message.getSourceTitle() : "",
                message.getSourceDesc() != null ? message.getSourceDesc() : "",
                message.getTargetUrl() != null ? message.getTargetUrl() : ""
        );

        try {
            broadcast(message.getConversationId(), serverMessage);
        } catch (IOException ignored) {
        }
    }
}
