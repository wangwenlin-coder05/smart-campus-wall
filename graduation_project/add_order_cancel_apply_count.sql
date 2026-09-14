-- Run this once on database `graduation_project`.
-- It is safe to run more than once. The table/database should use utf8mb4.

SET @db_name = DATABASE();

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN cancel_apply_count INT NOT NULL DEFAULT 0 COMMENT ''客户申请取消次数'' AFTER cancel_reason',
        'SELECT ''cancel_apply_count already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'cancel_apply_count'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE order_info ADD COLUMN lifecycle_log TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT ''订单生命流程日志'' AFTER cancel_apply_count',
        'SELECT ''lifecycle_log already exists'''
    )
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'order_info'
      AND COLUMN_NAME = 'lifecycle_log'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
