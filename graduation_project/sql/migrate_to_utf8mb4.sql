-- ============================================
-- 项目整体迁移至 utf8mb4（数据库 + 所有业务表 + 字符字段）
-- 执行方式：在 MySQL 命令行 / Navicat / DBeaver 中执行本脚本
-- 作用：确保数据库、表、字段均使用 utf8mb4，可完整存储 emoji（4字节字符）
-- ============================================

-- 1. 连接字符集声明
SET NAMES utf8mb4;

-- 2. 升级数据库默认字符集为 utf8mb4
ALTER DATABASE graduation_project
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE graduation_project;

-- ============================================
-- 3. 逐个升级业务表的默认字符集为 utf8mb4
--    并将所有 VARCHAR/TEXT 字段同步改为 utf8mb4
-- ============================================

-- 3.1 用户表
ALTER TABLE user
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 3.2 订单相关
ALTER TABLE order_info
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

ALTER TABLE order_image
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

ALTER TABLE order_category
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 3.3 地址表
ALTER TABLE user_address
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 3.4 职务相关
ALTER TABLE school_duty
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

ALTER TABLE user_duty_rel
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 3.5 帖子相关
ALTER TABLE wall_post
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

ALTER TABLE wall_post_action
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 3.6 聊天消息
ALTER TABLE chat_message
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- 3.7 OSS分片上传任务
ALTER TABLE upload_task
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- ============================================
-- 4. 验证：查看最终字符集（可独立执行）
-- ============================================
-- SELECT TABLE_NAME, TABLE_COLLATION
-- FROM information_schema.TABLES
-- WHERE TABLE_SCHEMA = 'graduation_project';

-- SELECT TABLE_NAME, COLUMN_NAME, CHARACTER_SET_NAME, COLLATION_NAME
-- FROM information_schema.COLUMNS
-- WHERE TABLE_SCHEMA = 'graduation_project'
--   AND DATA_TYPE IN ('char', 'varchar', 'tinytext', 'text', 'mediumtext', 'longtext', 'enum', 'set');

-- SELECT DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME
-- FROM information_schema.SCHEMATA
-- WHERE SCHEMA_NAME = 'graduation_project';
