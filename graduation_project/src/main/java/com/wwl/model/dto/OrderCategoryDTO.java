package com.wwl.model.dto;

import lombok.Data;


/**
 * OrderCategory请求参数DTO */
@Data
public class OrderCategoryDTO {
    /** 分类主键id */
    private Integer id;

    /** 分类名称 */
    private String catName;

    /**
     * 父级分类id
     * parentId = 0 代表一级顶级分�?
     * parentId = 对应一级id 代表二级子分�?
     */
    private Integer parentId;

    /** 是否删除 0-未删�?1-已删�?*/
    private Integer isDeleted;


    // ==================== 查询专用字段 ====================
    private Integer sceneType;
    private Integer pageNum;
    private Integer pageSize;
}