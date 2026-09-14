package com.wwl.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * WallCategorySub瑙嗗浘杩斿洖VO绫? */
@Data
public class WallCategorySubVO {
    @JsonIgnore
    private Long id;

    /**
     * 鍏宠仈涓?绾у垎绫籌D
     */
    private Long categoryId;

    /**
     * 瀛愬垎绫诲悕绉?
     */
    private String name;

    /**
     * 鎺掑簭(鍗囧簭闈犲墠)
     */
    private Integer sort;

    /**
     * 鍓嶇鏄惁灞曠ず 1鏄?鍚?
     */
    private Integer isShow;

}
