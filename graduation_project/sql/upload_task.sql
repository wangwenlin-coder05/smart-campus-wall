-- OSS分片上传任务表
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `upload_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键自增ID',
  `upload_id` VARCHAR(128) NOT NULL COMMENT 'OSS上传ID（由阿里云OSS生成）',
  `file_name` VARCHAR(255) NOT NULL COMMENT '文件原始名称',
  `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
  `content_type` VARCHAR(100) DEFAULT NULL COMMENT '文件MIME类型',
  `object_key` VARCHAR(500) NOT NULL COMMENT 'OSS存储路径（object key）',
  `user_uid` VARCHAR(64) NOT NULL COMMENT '用户UID',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '上传状态：0-初始化 1-上传中 2-已完成 3-已取消 4-已失败',
  `checkpoint` TEXT COMMENT '检查点数据（JSON格式，记录已上传的分片信息）',
  `uploaded_parts` INT NOT NULL DEFAULT 0 COMMENT '已上传分片数量',
  `total_parts` INT NOT NULL DEFAULT 0 COMMENT '总分片数量',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识：0-未删除 1-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `expire_time` DATETIME NOT NULL COMMENT '过期时间（超过此时间的未完成任务可被清理）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_upload_id` (`upload_id`),
  KEY `idx_user_uid` (`user_uid`),
  KEY `idx_status` (`status`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OSS分片上传任务表';
