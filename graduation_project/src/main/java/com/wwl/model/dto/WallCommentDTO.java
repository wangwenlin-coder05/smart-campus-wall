package com.wwl.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * WallComment请求参数DTO类
 */
@Data
public class WallCommentDTO {
    /**
     * 评论主键ID
     */
    private Long id;

    /**
     * 是否匿名评论：0-实名 1-匿名
     */
    private Integer isAnonymous;

    /**
     * 评论点赞数量
     */
    private Integer likeCount;

    /**
     * 父评论ID，0代表顶级评论
     */
    private Long parentId;

    /**
     * 回复目标评论ID
     */
    private Long replyCommentId;

    /**
     * 被回复用户脱敏字符串UID
     */
    private String replyUserUid;


    // ==================== 查询专用字段 ====================
    private Integer sceneType;
    private Integer pageNum;
    private Integer pageSize;
}
