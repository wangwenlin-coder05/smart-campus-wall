SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS lost_found_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '失物招领ID',
    type VARCHAR(16) NOT NULL COMMENT 'lost寻物/found招领',
    title VARCHAR(80) NOT NULL COMMENT '物品标题',
    description VARCHAR(500) NOT NULL COMMENT '描述',
    location VARCHAR(120) NOT NULL COMMENT '地点',
    image VARCHAR(255) NULL COMMENT '图片',
    publisher_uid VARCHAR(64) NOT NULL COMMENT '发布人UID',
    publisher_name VARCHAR(60) NOT NULL COMMENT '发布人姓名',
    publisher_phone VARCHAR(20) NULL COMMENT '发布人手机号',
    publisher_student_id VARCHAR(40) NULL COMMENT '发布人学号',
    status INT NOT NULL DEFAULT 0 COMMENT '0处理中 1已归还 2待领取 3已领取',
    views INT NOT NULL DEFAULT 0 COMMENT '浏览数',
    resolved_application_id BIGINT NULL COMMENT '完成申请ID',
    returner_name VARCHAR(50) NULL COMMENT '归还人姓名',
    claimer_name VARCHAR(50) NULL COMMENT '领取人姓名',
    return_time DATETIME NULL COMMENT '归还时间',
    claim_time DATETIME NULL COMMENT '领取时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_lost_found_status (status, is_deleted),
    KEY idx_lost_found_type (type),
    KEY idx_lost_found_publisher (publisher_uid),
    KEY idx_lost_found_keyword (title, location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='失物招领物品';

CREATE TABLE IF NOT EXISTS lost_found_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申请ID',
    item_id BIGINT NOT NULL COMMENT '物品ID',
    apply_type VARCHAR(16) NOT NULL COMMENT 'claim领取/return归还',
    applicant_uid VARCHAR(64) NOT NULL COMMENT '申请人UID',
    applicant_name VARCHAR(60) NOT NULL COMMENT '申请人姓名',
    applicant_phone VARCHAR(20) NOT NULL COMMENT '申请人电话',
    applicant_student_id VARCHAR(40) NOT NULL COMMENT '申请人学号',
    applicant_id_card VARCHAR(32) NULL COMMENT '申请人身份证号',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT 'pending待同意 approved已同意 rejected已拒绝 completed已完成',
    is_deleted TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_lost_found_app_item (item_id),
    KEY idx_lost_found_app_user (applicant_uid),
    KEY idx_lost_found_app_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='失物招领申请';

INSERT INTO lost_found_item
(id, type, title, description, location, image, publisher_uid, publisher_name, publisher_phone, publisher_student_id, status, views, resolved_application_id, returner_name, claimer_name, return_time, claim_time, is_deleted, create_time, update_time)
VALUES
(1, 'lost', '白色 AirPods Pro 丢失', '周三下午在图书馆三楼自习室遗失，外壳上有星星贴纸，捡到请联系。', '图书馆三楼自习室', '', '1001', '小陈同学', '13800000001', '2021001001', 0, 234, NULL, NULL, NULL, NULL, NULL, 0, '2026-06-10 16:20:00', NOW()),
(2, 'found', '捡到校园卡一张', '在一食堂门口捡到，卡号后四位 8821，失主看到后请尽快认领。', '一食堂门口', '', '1002', '热心学长', '13800000002', '2021001002', 0, 156, NULL, NULL, NULL, NULL, NULL, 0, '2026-06-10 12:05:00', NOW()),
(3, 'lost', '黑色保温杯遗失', '膳魔师黑色 500ml 保温杯，杯盖有轻微划痕，已由同学归还。', '教二 201', '', '1003', '小李', '13800000003', '2021001003', 1, 89, 3, '小王', NULL, '2026-06-09 18:30:00', NULL, 0, '2026-06-09 09:40:00', NOW()),
(4, 'found', '捡到一本算法导论', '在图书馆二楼归还车上发现，书内有少量笔记，已被失主领取。', '图书馆二楼', '', '1004', '书虫小王', '13800000004', '2021001004', 3, 312, 4, NULL, '小赵', NULL, '2026-06-09 10:10:00', 0, '2026-06-08 20:15:00', NOW()),
(5, 'lost', '蓝色雨伞忘在操场', '傍晚训练后忘在看台第一排，伞柄贴有姓名贴。', '操场看台', '', '1005', '小周', '13800000005', '2021001005', 0, 67, NULL, NULL, NULL, NULL, NULL, 0, '2026-06-11 08:30:00', NOW()),
(6, 'found', '捡到计算器', '在实验楼 302 桌洞里发现一台卡西欧计算器。', '实验楼 302', '', '1006', '实验室助教', '13800000006', '2021001006', 0, 45, NULL, NULL, NULL, NULL, NULL, 0, '2026-06-11 10:15:00', NOW()),
(7, 'found', '捡到白色充电宝', '在教学楼 A101 前排座位捡到，机身有一条银色贴纸。', '教学楼 A101', '', '1007', '值班同学', '13800000007', '2021001007', 0, 78, NULL, NULL, NULL, NULL, NULL, 0, '2026-06-11 11:25:00', NOW()),
(8, 'lost', '黑色钱包遗失', '中午在二食堂附近遗失，内有校园卡和少量现金。', '二食堂附近', '', '1008', '小吴', '13800000008', '2021001008', 0, 96, NULL, NULL, NULL, NULL, NULL, 0, '2026-06-11 12:40:00', NOW()),
(9, 'found', '捡到透明眼镜盒', '在文体中心二楼休息区捡到，已经由失主领取。', '文体中心二楼', '', '1009', '文体中心前台', '13800000009', '2021001009', 3, 64, 5, NULL, '小林', NULL, '2026-06-11 15:20:00', 0, '2026-06-11 14:10:00', NOW()),
(10, 'lost', '蓝色帆布袋遗失', '在快递站附近遗失，袋内有教材和笔记本，已由同学归还。', '快递站', '', '1010', '小郑', '13800000010', '2021001010', 1, 83, 6, '小何', NULL, '2026-06-11 16:05:00', NULL, 0, '2026-06-11 13:35:00', NOW())
ON DUPLICATE KEY UPDATE
    type = VALUES(type),
    title = VALUES(title),
    description = VALUES(description),
    location = VALUES(location),
    image = VALUES(image),
    publisher_uid = VALUES(publisher_uid),
    publisher_name = VALUES(publisher_name),
    publisher_phone = VALUES(publisher_phone),
    publisher_student_id = VALUES(publisher_student_id),
    status = VALUES(status),
    views = VALUES(views),
    resolved_application_id = VALUES(resolved_application_id),
    returner_name = VALUES(returner_name),
    claimer_name = VALUES(claimer_name),
    return_time = VALUES(return_time),
    claim_time = VALUES(claim_time),
    is_deleted = VALUES(is_deleted),
    create_time = VALUES(create_time),
    update_time = VALUES(update_time);

INSERT INTO lost_found_application
(id, item_id, apply_type, applicant_uid, applicant_name, applicant_phone, applicant_student_id, applicant_id_card, status, is_deleted, create_time, update_time)
VALUES
(1, 2, 'claim', '1001', '小陈同学', '13800000001', '2021001001', '110101200001010011', 'pending', 0, '2026-06-10 13:20:00', NOW()),
(2, 1, 'return', '1002', '热心学长', '13800000002', '2021001002', NULL, 'approved', 0, '2026-06-10 17:00:00', NOW()),
(3, 3, 'return', '1004', '小王', '13800000004', '2021001004', NULL, 'completed', 0, '2026-06-09 17:10:00', NOW()),
(4, 4, 'claim', '1005', '小赵', '13800000005', '2021001005', '110101200001010055', 'completed', 0, '2026-06-09 09:40:00', NOW()),
(5, 9, 'claim', '1011', '小林', '13800000011', '2021001011', '110101200001010111', 'completed', 0, '2026-06-11 15:00:00', NOW()),
(6, 10, 'return', '1012', '小何', '13800000012', '2021001012', NULL, 'completed', 0, '2026-06-11 15:40:00', NOW())
ON DUPLICATE KEY UPDATE
    item_id = VALUES(item_id),
    apply_type = VALUES(apply_type),
    applicant_uid = VALUES(applicant_uid),
    applicant_name = VALUES(applicant_name),
    applicant_phone = VALUES(applicant_phone),
    applicant_student_id = VALUES(applicant_student_id),
    applicant_id_card = VALUES(applicant_id_card),
    status = VALUES(status),
    is_deleted = VALUES(is_deleted),
    create_time = VALUES(create_time),
    update_time = VALUES(update_time);
