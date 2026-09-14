package com.wwl.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息列表页展示用的会话对象。
 * 由聊天记录按 conversationId 聚合而来。
 */
@Data
public class ChatConversationVO {
    /** 会话ID */
    private String id;

    /** 会话名称 */
    private String name;

    /** 会话头像 */
    private String avatar;

    /** 最新一条消息 */
    private String lastMsg;

    /** 前端直接展示的时间文本 */
    private String time;

    /** 排序时间，越新越靠前 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastTime;

    /** 未读数：当前简化版先返回0 */
    private Integer unread;

    /** 是否置顶：当前简化版先返回false */
    private Boolean isPinned;

    /** 会话类型 */
    private String type;

    /** 业务来源类型 */
    private String sourceType;

    /** 业务来源ID */
    private String sourceId;

    /** 来源标题 */
    private String sourceTitle;

    /** 来源描述 */
    private String sourceDesc;

    /** 来源跳转链接 */
    private String targetUrl;
}