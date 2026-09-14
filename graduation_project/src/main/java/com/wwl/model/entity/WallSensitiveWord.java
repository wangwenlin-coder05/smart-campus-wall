package com.wwl.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WallSensitiveWord {
    private Long id;

    /**
     * 鏁忔劅璇?
     */
    private String word;

    /**
     * 杞垹闄?
     */
    private Integer isDeleted;

    /**
     * 鍒涘缓鏃堕棿
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;
}
