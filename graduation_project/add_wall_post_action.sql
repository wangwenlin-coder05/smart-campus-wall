-- 帖子点赞/收藏操作表
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS wall_post_action (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_no VARCHAR(32) NOT NULL,
  user_uid VARCHAR(32) NOT NULL,
  action_type VARCHAR(16) NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_post_user_action(post_no, user_uid, action_type),
  KEY idx_user_action(user_uid, action_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
