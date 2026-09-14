package com.wwl.model.dto;

import lombok.Data;

/**
 * WallSensitiveWord请求参数DTO */
@Data
public class WallSensitiveWordDTO {
    /**
     * 敏感词
     */
    private String word;

    /**
     * 软删除
     */
    private Integer isDeleted;


    // ==================== 查询专用字段 ====================
    private Integer sceneType;
    private Integer pageNum;
    private Integer pageSize;
}