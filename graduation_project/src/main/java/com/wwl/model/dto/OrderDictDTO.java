package com.wwl.model.dto;

import lombok.Data;


/**
 * OrderDict请求参数DTO */
@Data
public class OrderDictDTO {
    /** 主键id */
    private Integer id;

    /** 一级分类：代取/帮买/互助 */
    private String parentType;

    /** 二级细分分类 */
    private String subType;

    /** 是否删除 0-未删�?1-已删�?*/
    private Integer isDeleted;


    // ==================== 查询专用字段 ====================
    private Integer sceneType;
    private Integer pageNum;
    private Integer pageSize;
}