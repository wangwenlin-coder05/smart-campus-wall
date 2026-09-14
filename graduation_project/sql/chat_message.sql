-- 聊天消息表
-- 使用方法：在 MySQL 的 graduation_project 数据库中执行本文件。
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息主键',
    conversation_id VARCHAR(100) NOT NULL COMMENT '会话ID',
    source_type VARCHAR(30) DEFAULT 'chat' COMMENT '来源类型：chat/group/goods/system',
    source_id VARCHAR(100) DEFAULT NULL COMMENT '业务来源ID',
    conversation_title VARCHAR(100) DEFAULT NULL COMMENT '会话标题',
    conversation_avatar VARCHAR(500) DEFAULT NULL COMMENT '会话头像',
    sender_id VARCHAR(100) NOT NULL COMMENT '发送人ID',
    sender_name VARCHAR(100) DEFAULT 'anonymous' COMMENT '发送人昵称',
    sender_avatar VARCHAR(500) DEFAULT NULL COMMENT '发送人头像',
    content VARCHAR(1000) NOT NULL COMMENT '消息内容',
    message_type VARCHAR(30) DEFAULT 'chat' COMMENT '消息类型：chat/system',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否删除：0未删除，1已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_conversation_time (conversation_id, create_time),
    INDEX idx_sender_id (sender_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';
