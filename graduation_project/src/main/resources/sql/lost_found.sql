-- 失物招领表
CREATE TABLE IF NOT EXISTS `lost_found_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` VARCHAR(20) NOT NULL COMMENT '类型：lost寻物/found招领',
  `title` VARCHAR(200) NOT NULL COMMENT '物品名称',
  `description` TEXT COMMENT '详细描述',
  `location` VARCHAR(500) COMMENT '丢失/捡到地点',
  `image` VARCHAR(1000) COMMENT '图片地址',
  `publisher_uid` VARCHAR(64) NOT NULL COMMENT '发布人UID',
  `publisher_name` VARCHAR(50) NOT NULL COMMENT '发布人姓名',
  `publisher_phone` VARCHAR(20) NOT NULL COMMENT '发布人手机号',
  `publisher_student_id` VARCHAR(50) NOT NULL COMMENT '发布人学号',
  `status` INT NOT NULL DEFAULT 0 COMMENT '状态：0待处理/1已归还/2待领取/3已领取',
  `views` INT NOT NULL DEFAULT 0 COMMENT '浏览量',
  `resolved_application_id` BIGINT COMMENT '最终处理的申请ID',
  `returner_name` VARCHAR(50) COMMENT '归还人姓名',
  `claimer_name` VARCHAR(50) COMMENT '领取人姓名',
  `return_time` DATETIME COMMENT '归还时间',
  `claim_time` DATETIME COMMENT '领取时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0否/1是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_publisher_uid` (`publisher_uid`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='失物招领物品表';

-- 失物招领申请表
CREATE TABLE IF NOT EXISTS `lost_found_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `item_id` BIGINT NOT NULL COMMENT '关联物品ID',
  `apply_type` VARCHAR(20) NOT NULL COMMENT '申请类型：claim领取/return归还',
  `applicant_uid` VARCHAR(64) NOT NULL COMMENT '申请人UID',
  `applicant_name` VARCHAR(50) NOT NULL COMMENT '申请人姓名',
  `applicant_phone` VARCHAR(20) NOT NULL COMMENT '申请人手机号',
  `applicant_student_id` VARCHAR(50) NOT NULL COMMENT '申请人学号',
  `applicant_id_card` VARCHAR(30) COMMENT '申请人身份证号（领取时必填）',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态：pending待审核/approved已同意/rejected已拒绝/completed已完成',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0否/1是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_applicant_uid` (`applicant_uid`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='失物招领申请表';
