package com.wwl.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * WallReport瑙嗗浘杩斿洖VO绫? */
@Data
public class WallReportVO {
    @JsonIgnore
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

}
