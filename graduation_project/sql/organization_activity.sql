CREATE TABLE IF NOT EXISTS organization_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '组局ID',
    title VARCHAR(80) NOT NULL COMMENT '组局标题',
    category VARCHAR(40) NOT NULL COMMENT '大分类',
    tag_id INT NULL COMMENT '标签ID',
    tag_name VARCHAR(40) NULL COMMENT '标签名称',
    activity_time VARCHAR(40) NOT NULL COMMENT '活动时间展示文本',
    address VARCHAR(120) NOT NULL COMMENT '集合地点',
    max_people INT NOT NULL DEFAULT 2 COMMENT '人数上限',
    join_num INT NOT NULL DEFAULT 0 COMMENT '已报名人数',
    gender_limit VARCHAR(20) NOT NULL DEFAULT '不限' COMMENT '性别限制',
    fee_type VARCHAR(20) NOT NULL DEFAULT '免费' COMMENT '费用方式',
    deposit_required TINYINT NOT NULL DEFAULT 0 COMMENT '是否需要押金',
    deposit_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '押金金额',
    host_desc VARCHAR(255) NULL COMMENT '主理人说明',
    poster_img VARCHAR(255) NOT NULL DEFAULT '/static/organization/feature-card.jpg' COMMENT '封面图',
    brand_name VARCHAR(60) NOT NULL DEFAULT '校园搭子' COMMENT '组织者昵称',
    brand_avatar VARCHAR(255) NOT NULL DEFAULT '/static/default-avatar.png' COMMENT '组织者头像',
    group_count INT NOT NULL DEFAULT 1 COMMENT '组织者组局次数',
    qrcode_url VARCHAR(255) NOT NULL DEFAULT '/static/11/1.png' COMMENT '报名二维码',
    creator_user_id VARCHAR(64) NOT NULL DEFAULT 'system' COMMENT '发布人用户ID',
    status VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态 active/review/closed',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_org_activity_status (status, is_deleted),
    KEY idx_org_activity_creator (creator_user_id),
    KEY idx_org_activity_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='组局开黑活动';

CREATE TABLE IF NOT EXISTS organization_activity_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报名ID',
    activity_id BIGINT NOT NULL COMMENT '组局ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    nickname VARCHAR(60) NOT NULL DEFAULT '报名同学' COMMENT '昵称',
    avatar VARCHAR(255) NOT NULL DEFAULT '/static/default-avatar.png' COMMENT '头像',
    status VARCHAR(20) NOT NULL DEFAULT 'joined' COMMENT '状态 joined/cancelled',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_activity_user (activity_id, user_id),
    KEY idx_member_user (user_id),
    KEY idx_member_activity_status (activity_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='组局开黑报名成员';

INSERT INTO organization_activity
(id, title, category, tag_id, tag_name, activity_time, address, max_people, join_num, gender_limit, fee_type, deposit_required, deposit_amount, host_desc, poster_img, brand_name, brand_avatar, group_count, qrcode_url, creator_user_id, status, is_deleted)
VALUES
(1, '今晚五排开黑补两位', '休闲玩乐', 68, '电竞开黑', '今天 22:00', '线上语音房', 5, 3, '不限', '免费', 0, 0, '快乐上分，拒绝压力队友，会开麦就行。', '/static/organization/feature-card.jpg', '峡谷开黑队', '/static/organization/feature-card.jpg', 21, '/static/11/1.png', '1001', 'active', 0),
(2, '周五火锅拼桌缺两位', '美食觅食', 41, '火锅', '周五 18:30', '松北路 4 号', 8, 2, '不限', 'AA', 1, 10, '提前占位，口味可商量，AA 透明。', '/static/organization/feature-card.jpg', '火锅搭子集合', '/static/organization/feature-card.jpg', 6, '/static/11/1.png', '1002', 'active', 0),
(3, '图书馆番茄钟自习', '学业备考', 21, '自习', '明天 09:00', '图书馆 3F', 16, 1, '不限', '免费', 0, 0, '互相监督不闲聊，午饭和晚饭时间统一休息。', '/static/organization/feature-card.jpg', '图书馆自习局', '/static/organization/feature-card.jpg', 14, '/static/11/1.png', '1003', 'active', 0),
(4, '操场夜跑打卡', '体育运动', 10, '跑步', '今天 20:10', '东区操场', 12, 1, '不限', '免费', 0, 0, '配速不卷，跑完一起拉伸，新手也能跟。', '/static/organization/feature-card.jpg', '操场夜跑团', '/static/organization/feature-card.jpg', 9, '/static/11/1.png', '1004', 'active', 0)
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    category = VALUES(category),
    tag_id = VALUES(tag_id),
    tag_name = VALUES(tag_name),
    activity_time = VALUES(activity_time),
    address = VALUES(address),
    max_people = VALUES(max_people),
    join_num = VALUES(join_num),
    gender_limit = VALUES(gender_limit),
    fee_type = VALUES(fee_type),
    deposit_required = VALUES(deposit_required),
    deposit_amount = VALUES(deposit_amount),
    host_desc = VALUES(host_desc),
    poster_img = VALUES(poster_img),
    brand_name = VALUES(brand_name),
    brand_avatar = VALUES(brand_avatar),
    group_count = VALUES(group_count),
    qrcode_url = VALUES(qrcode_url),
    creator_user_id = VALUES(creator_user_id),
    status = VALUES(status),
    is_deleted = VALUES(is_deleted);

INSERT INTO organization_activity_member (activity_id, user_id, nickname, avatar, status)
VALUES
(1, '2001', '报名同学', '/static/default-avatar.png', 'joined'),
(1, '2002', '报名同学', '/static/default-avatar.png', 'joined'),
(1, '2003', '报名同学', '/static/default-avatar.png', 'joined'),
(2, '2004', '报名同学', '/static/default-avatar.png', 'joined'),
(2, '2005', '报名同学', '/static/default-avatar.png', 'joined'),
(3, '2006', '报名同学', '/static/default-avatar.png', 'joined'),
(4, '2007', '报名同学', '/static/default-avatar.png', 'joined')
ON DUPLICATE KEY UPDATE status = VALUES(status);
