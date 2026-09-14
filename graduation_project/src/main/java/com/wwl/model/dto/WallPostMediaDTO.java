package com.wwl.model.dto;

import lombok.Data;

/**
 * WallPostMedia请求参数DTO */
@Data
public class WallPostMediaDTO {

    // ==================== 查询专用字段 ====================
    private Integer sceneType;
    private Integer pageNum;
    private Integer pageSize;
}