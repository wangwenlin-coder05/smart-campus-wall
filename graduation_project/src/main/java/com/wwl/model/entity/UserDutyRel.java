package com.wwl.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 鐢ㄦ埛-鏍″洯鑱屽姟 澶氬澶氬叧鑱斾腑闂磋〃
 * 鍙瓨鍏崇郴锛屾棤涓氬姟瀛楁锛屼笉鍋氫慨鏀规搷浣?
 */
@Data
public class UserDutyRel {

    /** 涓婚敭id */
    private Long id;

    /** 鐢ㄦ埛id */
    private Long userId;

    /** 鑱屽姟id 瀵瑰簲 school_duty 琛╥d */
    private Integer dutyId;

    /** 鏄惁鍒犻櫎 0-鏈垹闄?1-宸插垹闄?*/
    private Integer isDeleted;

    /** 鍏宠仈鍒涘缓鏃堕棿 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;
}
