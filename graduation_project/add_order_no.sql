-- 为 order_info 表添加对外订单号字段
SET NAMES utf8mb4;

ALTER TABLE order_info 
ADD COLUMN order_no VARCHAR(32) NOT NULL COMMENT '对外订单编号' AFTER id;

-- 为 order_no 字段添加唯一索引，保证订单号不重复
CREATE UNIQUE INDEX idx_order_no ON order_info(order_no);
