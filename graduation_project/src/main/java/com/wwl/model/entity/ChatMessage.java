package com.wwl.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类。
 * 一条记录对应用户在某个会话里发送的一条消息。
 */
@Data
public class ChatMessage {
    /** 消息主键 */
    private Long id;

    /** 会话ID：同一个会话ID下的消息会显示在同一个聊天窗口 */
    private String conversationId;

    /** 会话来源类型：chat普通聊天、group组局、goods商品、system系统 */
    private String sourceType;

    /** 业务来源ID：例如商品ID、组局ID、用户ID */
    private String sourceId;

    /** 来源标题：例如组局活动标题、商品名称 */
    private String sourceTitle;

    /** 来源描述：例如活动时间+地址 */
    private String sourceDesc;

    /** 来源跳转链接：点击来源卡片跳转的目标页面 */
    private String targetUrl;

    /** 会话标题，方便消息列表展示 */
    private String conversationTitle;

    /** 会话头像，方便消息列表展示 */
    private String conversationAvatar;

    /** 发送人ID，前端用它判断消息显示在左侧还是右侧 */
    private String senderId;

    /** 发送人昵称 */
    private String senderName;

    /** 发送人头像 */
    private String senderAvatar;

    /** 消息内容 */
    private String content;

    /** 消息类型：chat聊天消息、system系统消息 */
    private String messageType;

    /** 是否删除：0未删除，1已删除 */
    private Integer isDeleted;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}