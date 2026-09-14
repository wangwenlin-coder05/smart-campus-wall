package com.wwl.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 表白墙评论实体类
 * 对应数据表：wall_comment
 */
@Data
public class WallComment {

    /**
     * 评论主键ID
     */
    private Long id;

    /**
     * 关联的帖子ID
     */
    @NotNull(message = "关联帖子ID不能为空")
    private Long postId;

    /**
     * 评论用户数字主键ID
     * 内部数据库关联使用，不返回前端
     * 前端不传递这个字段，后端自己根据 userUid 赋值
     */
    @JsonIgnore
    private Long userId;

    /**
     * 用户对外脱敏字符串UID
     * 前端传入，交互专用
     */
    @NotBlank(message = "用户标识不能为空")
    private String userUid;

    /**
     * 是否匿名评论 0-实名 1-匿名
     */
    private Integer isAnonymous;

    /**
     * 评论文字内容
     */
    private String content;

    /**
     * 媒体资源（图片）URL列表
     * 逗号分隔或者JSON存储，前端直传OSS后保存链接
     */
    private String mediaUrls;

    /**
     * 评论点赞数量
     */
    private Integer likeCount;

    /**
     * 父评论ID 0代表顶级评论
     */
    private Long parentId;

    /**
     * 回复目标评论ID
     */
    private Long replyCommentId;

    /**
     * 被回复用户数字主键ID
     * 内部使用，不返回前端，前端不传递
     */
    @JsonIgnore
    private Long replyUserId;

    /**
     * 被回复用户脱敏字符串UID
     */
    private String replyUserUid;

    /**
     * 软删除标识：0-正常 1-已删除
     */
    @JsonIgnore
    private Integer isDeleted;

    /**
     * 评论创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 评论更新时间
     */
    @JsonIgnore
    private LocalDateTime updateTime;

    // 联表查询扩展字段，数据库不存储
    private String nickname;
    private String avatar;
    private String replyNickname;
}