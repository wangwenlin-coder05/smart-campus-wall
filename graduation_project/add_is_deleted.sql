-- ==========================================
-- 为所有业务表添加软删除字段 is_deleted
SET NAMES utf8mb4;
-- 0-未删除（正常） 1-已删除
-- ==========================================

-- 1. order_info 订单表
ALTER TABLE order_info 
ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除' AFTER order_status;

-- 2. user_address 用户地址表
ALTER TABLE user_address 
ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除' AFTER is_default;

-- 3. order_image 订单图片表
ALTER TABLE order_image 
ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除' AFTER image_url;

-- 4. user 用户表
ALTER TABLE user 
ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除' AFTER status;

-- 5. school_duty 校园职务表
ALTER TABLE school_duty 
ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除' AFTER duty_desc;

-- 6. user_duty_rel 用户职务关联表
ALTER TABLE user_duty_rel 
ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除' AFTER duty_id;

-- 7. order_category 订单分类表（如果存在）
ALTER TABLE order_category
ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除' AFTER parent_id;


# ==========================================
# 验证是否成功
# ==========================================
DESC order_info;
DESC user_address;
DESC order_image;
DESC user;
DESC school_duty;
DESC user_duty_rel;
DESC order_category;

