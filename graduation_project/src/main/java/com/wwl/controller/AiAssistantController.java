package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.model.dto.AiChatDTO;
import com.wwl.service.AiAssistantService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 助手接口控制器
 * 
 * 暴露 HTTP 接口给前端 uni-app 调用。
 * 所有接口路径以 "/ai" 开头。
 */
@RestController
@RequestMapping("/ai")
public class AiAssistantController {

    /** 注入 AI 服务 */
    private final AiAssistantService aiAssistantService;

    /** 构造注入，Spring 自动完成 */
    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    /**
     * AI 对话接口
     * 
     * 请求方式：POST /ai/chat
     * 请求体：{ "message": "你的问题" }
     * 响应：{ "code": 1, "data": "AI 回答内容" }
     * 
     * 工作流程：
     * 1. 前端发问题 → 2. 参数校验 → 3. 调用 AiAssistantService.chat() → 4. 返回结果
     */
    @PostMapping("/chat")
    public Result chat(@RequestBody AiChatDTO dto) {
        // 参数校验：消息不能为空
        if (dto.getMessage() == null || dto.getMessage().isBlank()) {
            return Result.error("消息不能为空");
        }
        // 调用 AI 服务，获取回答
        String reply = aiAssistantService.chat(dto.getMessage());
        // 返回统一格式给前端
        return Result.success(reply);
    }
}