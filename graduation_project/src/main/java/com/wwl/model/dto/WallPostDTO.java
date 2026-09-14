package com.wwl.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

/**
 * WallPost请求参数DTO类
 */
@Data
public class WallPostDTO {
    /**
     * 帖子ID
     */
    private Long id;

    /**
     * 发布用户UID（关联查询返回）
     */
    private String userUid;

    /**
     * 一级分类ID
     */
    private Long categoryId;

    /**
     * 二级子分类ID
     */
    private Long subCategoryId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 0实名 1匿名
     */
    private Integer isAnonymous;

    /**
     * 帖子文字内容
     */
    private String content;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 帖子点赞数
     */
    private Integer likeCount;

    /**
     * 收藏数
     */
    private Integer collectCount;

    /**
     * 评论总数
     */
    private Integer commentCount;

    /**
     * 0待审核 1审核通过 2驳回
     */
    private Integer status;

    /**
     * 是否置顶 0否 1是
     */
    private Integer isTop;

    /**
     * 0立即发布 1定时发布
     */
    private Integer isTiming;

    /**
     * 软删除 0正常 1删除
     */
    private Integer isDeleted;

    /**
     * 图片/视频/音频混合资源地址
     * 前端上传完拼接逗号分隔字符串，最多9个
     */
    private List<String> mediaUrlList;

    // ==================== 查询专用字段 ====================
    private Integer sceneType;
    private Integer pageNum;
    private Integer pageSize;
}
