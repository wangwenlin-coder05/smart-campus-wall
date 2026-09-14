package com.wwl.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WallCategory {
    /**
     * 涓?绾у垎绫籌D
     */
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

    /**
     * 杞垹闄?0姝ｅ父1鍒犻櫎
     */
    private Integer isDeleted;

    /**
     * 鍒涘缓鏃堕棿
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 鏇存柊鏃堕棿
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
