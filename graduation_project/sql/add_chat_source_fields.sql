-- 为 chat_message 表添加来源信息字段
-- 使用方法：在 MySQL 的 graduation_project 数据库中执行本文件，可重复执行。
SET NAMES utf8mb4;

SET @db_name = DATABASE();

-- 添加 source_title 字段
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE chat_message ADD COLUMN source_title VARCHAR(200) DEFAULT NULL COMMENT ''来源标题'' AFTER source_id',
        'SELECT ''source_title already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'chat_message' AND COLUMN_NAME = 'source_title'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 source_desc 字段
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE chat_message ADD COLUMN source_desc VARCHAR(500) DEFAULT NULL COMMENT ''来源描述'' AFTER source_title',
        'SELECT ''source_desc already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'chat_message' AND COLUMN_NAME = 'source_desc'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 target_url 字段
SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE chat_message ADD COLUMN target_url VARCHAR(500) DEFAULT NULL COMMENT ''来源跳转链接'' AFTER source_desc',
        'SELECT ''target_url already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name AND TABLE_NAME = 'chat_message' AND COLUMN_NAME = 'target_url'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;