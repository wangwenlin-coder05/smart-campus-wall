package com.wwl.service.impl;

import com.wwl.mapper.ChatMessageMapper;
import com.wwl.model.entity.ChatMessage;
import com.wwl.model.vo.ChatConversationVO;
import com.wwl.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 聊天消息业务实现。
 */
@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd");

    /**
     * 清理重复会话：同一组参与者只保留最新一条。
     * 不同入口（组局招呼、用户主页、好友聊天）可能生成不同 conversation_id，
     * 这里按实际 sender_id 集合去重，保证每对用户只有一个会话。
     * @return 清理的会话数量
     */
    @Override
    public int cleanupDuplicateConversations() {
        int totalDeleted = 0;

        // 1. 获取所有会话及其参与者
        List<Map<String, Object>> allConvs = chatMessageMapper.findDuplicateConversationIds();
        if (allConvs == null || allConvs.isEmpty()) {
            return 0;
        }

        // 2. 按参与者集合分组，每组只保留最新（latestTime 最大）的一条
        Map<String, List<Map<String, Object>>> participantGroups = new LinkedHashMap<>();
        for (Map<String, Object> conv : allConvs) {
            String participants = String.valueOf(conv.getOrDefault("participants", ""));
            if (participants.isEmpty()) continue;
            participantGroups.computeIfAbsent(participants, k -> new ArrayList<>()).add(conv);
        }

        List<String> toDelete = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : participantGroups.entrySet()) {
            List<Map<String, Object>> convs = entry.getValue();
            if (convs.size() <= 1) continue;

            // 按 latestTime 降序，保留第一条，删除其余
            convs.sort((a, b) -> {
                Object ta = a.get("lastTime");
                Object tb = b.get("lastTime");
                if (ta == null && tb == null) return 0;
                if (ta == null) return 1;
                if (tb == null) return -1;
                String sa = String.valueOf(ta);
                String sb = String.valueOf(tb);
                return sb.compareTo(sa);
            });
            for (int i = 1; i < convs.size(); i++) {
                toDelete.add(String.valueOf(convs.get(i).get("id")));
            }
        }

        if (!toDelete.isEmpty()) {
            chatMessageMapper.batchDeleteConversations(toDelete);
            totalDeleted += toDelete.size();
        }

        // 3. 清理"失物招领"相关会话
        List<String> lostFoundIds = chatMessageMapper.findConversationIdsByTitle("失物招领");
        if (lostFoundIds != null && !lostFoundIds.isEmpty()) {
            chatMessageMapper.batchDeleteConversations(lostFoundIds);
            totalDeleted += lostFoundIds.size();
        }

        return totalDeleted;
    }

    @Override
    public ChatMessage saveMessage(ChatMessage chatMessage) {
        // 这些默认值放在后端兜底，前端少传字段时也不影响入库。
        if (chatMessage.getMessageType() == null || chatMessage.getMessageType().isBlank()) {
            chatMessage.setMessageType("chat");
        }
        if (chatMessage.getSourceType() == null || chatMessage.getSourceType().isBlank()) {
            chatMessage.setSourceType("chat");
        }
        if (chatMessage.getSourceId() == null || chatMessage.getSourceId().isBlank()) {
            chatMessage.setSourceId(chatMessage.getConversationId());
        }
        if (chatMessage.getConversationTitle() == null || chatMessage.getConversationTitle().isBlank()) {
            chatMessage.setConversationTitle("消息对话");
        }
        if (chatMessage.getConversationAvatar() == null) {
            chatMessage.setConversationAvatar("");
        }
        if (chatMessage.getSenderAvatar() == null) {
            chatMessage.setSenderAvatar("");
        }
        chatMessage.setIsDeleted(0);
        chatMessage.setCreateTime(LocalDateTime.now());

        chatMessageMapper.insert(chatMessage);
        return chatMessage;
    }

    @Override
    public List<ChatMessage> listHistory(String conversationId, Integer limit) {
        int size = limit == null ? 50 : Math.max(1, Math.min(limit, 100));
        return chatMessageMapper.listByConversationId(conversationId, size);
    }

    @Override
    public List<ChatConversationVO> listConversation(String userId) {
        List<ChatConversationVO> list = chatMessageMapper.listConversation(userId);
        for (ChatConversationVO item : list) {
            if (item.getUnread() == null) {
                item.setUnread(0);
            }
            item.setIsPinned(false);
            item.setTime(formatListTime(item.getLastTime()));
        }
        return list;
    }

    @Override
    public void markRead(String conversationId, String userId) {
        if (conversationId == null || conversationId.isBlank() || userId == null || userId.isBlank()) {
            return;
        }
        chatMessageMapper.markRead(conversationId, userId);
    }

    @Override
    public void deleteConversation(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return;
        }
        chatMessageMapper.deleteConversation(conversationId);
    }

    @Override
    public boolean deleteMessage(Long id) {
        if (id == null) {
            return false;
        }
        return chatMessageMapper.deleteById(id) > 0;
    }

    /**
     * 消息列表时间展示：今天显示时分，非今天显示月/日。
     */
    private String formatListTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        if (LocalDate.now().equals(time.toLocalDate())) {
            return time.format(TIME_FORMATTER);
        }
        return time.format(DATE_FORMATTER);
    }
}