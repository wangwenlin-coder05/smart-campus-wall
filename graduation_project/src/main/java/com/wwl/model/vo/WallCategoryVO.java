package com.wwl.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * WallCategory瑙嗗浘杩斿洖VO绫? */
@Data
public class WallCategoryVO {
    @JsonIgnore
    private Long id;

    /**
     * 鍒嗙被鍚嶇О
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
