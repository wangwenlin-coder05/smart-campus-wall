package com.wwl.model.dto;

import lombok.Data;

/**
 * WallCategorySub请求参数DTO */
@Data
public class WallCategorySubDTO {
    /**
     * 二级子分类ID
     */
    private Long id;

    /**
     * 关联一级分类ID
     */
    private Long categoryId;

    /**
     * 子分类名称
     */
    private String name;

    /**
     * 排序(升序靠前)
     */
    private Integer sort;

    /**
     * 前端是否展示 1是 0否
     */
    private Integer isShow;

    /**
     * 软删除
     */
    private Integer isDeleted;


    // ==================== 查询专用字段 ====================
    private Integer sceneType;
    private Integer pageNum;
    private Integer pageSize;
}