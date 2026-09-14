package com.wwl.model.dto;

import lombok.Data;

/**
 * WallReport请求参数DTO */
@Data
public class WallReportDTO {
    /**
     * 举报人ID
     */
    private Long userId;

    /**
     * 举报帖子ID
     */
    private Long postId;

    /**
     * 举报评论ID
     */
    private Long commentId;

    /**
     * 举报类型
     */
    private Integer reportType;

    /**
     * 举报原因
     */
    private String reason;

    /**
     * 0待处�?1已处�?2驳回
     */
    private Integer status;

    /**
     * 软删除
     */
    private Integer isDeleted;


    // ==================== 查询专用字段 ====================
    private Integer sceneType;
    private Integer pageNum;
    private Integer pageSize;
}