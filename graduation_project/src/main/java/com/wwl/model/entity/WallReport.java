package com.wwl.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WallReport {
    private Long id;

    /**
     * 涓炬姤浜篒D
     */
    private Long userId;

    /**
     * 涓炬姤甯栧瓙ID
     */
    private Long postId;

    /**
     * 涓炬姤璇勮ID
     */
    private Long commentId;

    /**
     * 涓炬姤绫诲瀷
     */
    private Integer reportType;

    /**
     * 涓炬姤鍘熷洜
     */
    private String reason;

    /**
     * 0寰呭鐞?1宸插鐞?2椹冲洖
     */
    private Integer status;

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
