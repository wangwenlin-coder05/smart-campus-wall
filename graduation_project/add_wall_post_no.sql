-- 为 wall_post 添加公开帖子编号字段
SET NAMES utf8mb4;

ALTER TABLE wall_post ADD COLUMN post_no VARCHAR(32) NULL COMMENT '公开帖子编号' AFTER id;

UPDATE wall_post
SET post_no = CONCAT('P', DATE_FORMAT(IFNULL(create_time, NOW()), '%Y%m%d%H%i%s'), LPAD(id, 6, '0'))
WHERE post_no IS NULL OR post_no = '';

ALTER TABLE wall_post MODIFY post_no VARCHAR(32) NOT NULL COMMENT '公开帖子编号';

CREATE UNIQUE INDEX uk_wall_post_post_no ON wall_post(post_no);
