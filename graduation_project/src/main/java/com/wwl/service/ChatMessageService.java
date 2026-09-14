package com.wwl.service;

import com.wwl.model.entity.ChatMessage;
import com.wwl.model.vo.ChatConversationVO;

import java.util.List;

/**
 * 聊天消息业务接口。
 */
public interface ChatMessageService {

    /** 保存聊天消息 */
    ChatMessage saveMessage(ChatMessage chatMessage);

    /** 获取会话历史消息 */
    List<ChatMessage> listHistory(String conversationId, Integer limit);

    /** 获取消息列表页会话 */
    List<ChatConversationVO> listConversation(String userId);

    void markRead(String conversationId, String userId);

    void deleteConversation(String conversationId);

    /** 清理重复的好友会话：同一用户对只保留最新一条，返回清理数量 */
    int cleanupDuplicateConversations();

    /** 软删除单条消息 */
    boolean deleteMessage(Long id);
}