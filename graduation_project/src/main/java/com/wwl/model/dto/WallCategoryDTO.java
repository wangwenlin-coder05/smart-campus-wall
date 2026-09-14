package com.wwl.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * WallCategory请求参数DTO */
@Data
public class WallCategoryDTO {
    /**
     * 一级分类ID
     */
    private Long id;

    /**
     * 分类名称
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
     * 软删除 0正常 1删除
     */
    private Integer isDeleted;


    // ==================== 查询专用字段 ====================
    private Integer sceneType;
    private Integer pageNum;
    private Integer pageSize;
}