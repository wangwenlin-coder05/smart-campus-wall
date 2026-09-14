package com.wwl.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * SchoolDuty璇锋眰鍙傛暟DTO绫? */
@Data
public class SchoolDutyDTO {
    /**
     * 鑱屽姟ID锛屼富閿嚜澧?
     */
    private Integer id;

    /**
     * 鑱屽姟鍚嶇О锛屽锛氱彮闀裤?佸洟鏀功銆佸鐢熶細骞蹭簨绛?
     */
    private String dutyName;

    /**
     * 鑱屽姟璇存槑锛屾弿杩拌亴鍔＄殑鑱岃矗鍜屽伐浣滃唴瀹?
     */
    private String dutyDesc;

    /**
     * 鏄惁鍒犻櫎 0-鏈垹闄?1-宸插垹闄?
     */
    private Integer isDeleted;


    // ==================== 鏌ヨ涓撶敤瀛楁 ====================
    private Integer sceneType;
    private Integer pageNum;
    private Integer pageSize;
}
