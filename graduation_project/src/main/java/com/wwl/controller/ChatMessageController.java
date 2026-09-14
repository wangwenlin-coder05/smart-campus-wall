package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.model.entity.ChatMessage;
import com.wwl.model.vo.ChatConversationVO;
import com.wwl.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 聊天相关普通 HTTP 接口。
 * WebSocket 负责实时收发，这里负责历史记录和消息列表。
 */
@RestController
@RequestMapping("/chat")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    /**
     * 获取消息列表页的最近会话。
     */
    @GetMapping("/conversations")
    public Result<List<ChatConversationVO>> conversations(@RequestParam(required = false) String userId) {
        return Result.success(chatMessageService.listConversation(userId));
    }

    /**
     * 获取某个会话的历史消息。
     */
    @GetMapping("/history")
    public Result<List<ChatMessage>> history(@RequestParam("conversationId") String conversationId,
                                             @RequestParam(value = "limit", required = false) Integer limit) {
        return Result.success(chatMessageService.listHistory(conversationId, limit));
    }

    @PostMapping("/read")
    public Result<String> read(@RequestParam("conversationId") String conversationId,
                               @RequestParam("userId") String userId) {
        chatMessageService.markRead(conversationId, userId);
        return Result.success("已读");
    }

    @DeleteMapping("/conversation/delete")
    public Result<String> deleteConversation(@RequestParam("conversationId") String conversationId) {
        chatMessageService.deleteConversation(conversationId);
        return Result.success("已删除");
    }

    @DeleteMapping("/message/{id}")
    public Result<String> deleteMessage(@PathVariable("id") Long id) {
        boolean ok = chatMessageService.deleteMessage(id);
        return ok ? Result.success("已删除") : Result.error(500, "删除失败");
    }

    /**
     * 清理重复的好友会话：同一用户对只保留最新一条。
     * 同时清理"失物招领通知"等脏数据。
     */
    @PostMapping("/conversations/cleanup")
    public Result<Integer> cleanupConversations() {
        int count = chatMessageService.cleanupDuplicateConversations();
        return Result.success(count);
    }
}