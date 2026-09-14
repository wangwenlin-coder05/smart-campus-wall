package com.wwl.model.dto;

import lombok.Data;

/**
 * 前端发送给 AI 的请求体
 * 
 * 前端 POST /ai/chat 时，请求体 JSON 格式：
 *   { "message": "图书馆几点关门？" }
 * 
 * Spring 会自动将 JSON 反序列化为此对象：
 *   message = "图书馆几点关门？"
 */
@Data
public class AiChatDTO {
    /** 用户发给 AI 的消息内容 */
    private String message;
}