package com.wwl.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;


/**
 * OrderDict瑙嗗浘杩斿洖VO绫? */
@Data
public class OrderDictVO {
    @JsonIgnore
    private Long id;

    /** 涓?绾у垎绫伙細浠ｅ彇/甯拱/浜掑姪 */
    private String parentType;

    /** 浜岀骇缁嗗垎鍒嗙被 */
    private String subType;

}
