package com.wwl.model.dto;

import lombok.Data;

/**
 * OrderImage请求参数DTO */
@Data
public class OrderImageDTO {
    /** 主键自增id */
    private Long id;

    /** 关联订单id，关联order_info订单主表 */
    private Long orderId;

    /** 阿里云图片在线访问链接地址 */
    private String imageUrl;

    /** 是否删除 0-未删除 1-已删除 */
    private Integer isDeleted;


    // ==================== 查询专用字段 ====================
    private Integer sceneType;
    private Integer pageNum;
    private Integer pageSize;
}