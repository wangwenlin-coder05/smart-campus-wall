package com.wwl.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * UserDutyRel璇锋眰鍙傛暟DTO绫? */
@Data
public class UserDutyRelDTO {
    /** 涓婚敭id */
    private Long id;

    /** 鐢ㄦ埛id */
    private Long userId;

    /** 鑱屽姟id 瀵瑰簲 school_duty 琛╥d */
    private Integer dutyId;

    /** 鏄惁鍒犻櫎 0-鏈垹闄?1-宸插垹闄?*/
    private Integer isDeleted;


    // ==================== 鏌ヨ涓撶敤瀛楁 ====================
    private Integer sceneType;
    private Integer pageNum;
    private Integer pageSize;
}
