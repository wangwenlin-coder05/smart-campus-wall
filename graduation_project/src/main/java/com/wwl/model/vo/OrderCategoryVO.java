package com.wwl.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;


/**
 * OrderCategory瑙嗗浘杩斿洖VO绫? */
@Data
public class OrderCategoryVO {
    @JsonIgnore
    private Long id;

    /** 鍒嗙被鍚嶇О */
    private String catName;

    /**
     * 鐖剁骇鍒嗙被id
     * parentId = 0 浠ｈ〃涓?绾ч《绾у垎绫?
     * parentId = 瀵瑰簲涓?绾d 浠ｈ〃浜岀骇瀛愬垎绫?
     */
    private Integer parentId;

}
