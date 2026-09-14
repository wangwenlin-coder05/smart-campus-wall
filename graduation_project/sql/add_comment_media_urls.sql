-- 评论表添加图片字段
ALTER TABLE wall_comment
    ADD COLUMN media_urls VARCHAR(500) NULL COMMENT '媒体资源URL（图片链接）' AFTER content;

ALTER TABLE wall_comment
    ADD COLUMN media_urls VARCHAR(500) NULL COMMENT '媒体资源URL（图片链接）' AFTER content;