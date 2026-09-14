-- 登录注册字段升级脚本
-- 支持手机号、邮箱、微信、支付宝四种登录/注册方式。
SET NAMES utf8mb4;

ALTER TABLE user
    MODIFY phone VARCHAR(20) NULL COMMENT '手机号，登录账号';

ALTER TABLE user
    ADD COLUMN email VARCHAR(100) NULL COMMENT '邮箱账号' AFTER phone;

ALTER TABLE user
    ADD COLUMN alipay_account VARCHAR(100) NULL COMMENT '支付宝账号' AFTER wechat;

CREATE UNIQUE INDEX idx_user_email ON user(email);
CREATE UNIQUE INDEX idx_user_alipay ON user(alipay_account);
CREATE UNIQUE INDEX idx_user_wechat ON user(wechat);
