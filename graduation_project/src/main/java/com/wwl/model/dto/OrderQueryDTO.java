package com.wwl.model.dto;

import lombok.Data;

@Data
public class OrderQueryDTO {
    /**
     * 页码，默认第1页
     */
    private Long pageNum = 1L;

    /**
     * 每页条数，默认20条
     */
    private Long pageSize = 20L;

    /**
     * 一级类型筛选
     */
    private String parentType;

    /**
     * 细分类型筛选
     */
    private String subType;

    /**
     * 排序字段 数据库字段名
     * 可选：create_time、reward_price、order_price、expect_time
     */
    private String sortField;

    /**
     * 排序方式 asc升序 / desc降序
     */
    private String sortOrder;
}
