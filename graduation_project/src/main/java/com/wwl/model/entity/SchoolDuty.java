package com.wwl.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 鏍″洯鑱屽姟瀹炰綋绫?
 * 瀵瑰簲鏁版嵁搴?school_duty 琛紝瀛樺偍鏍″洯鑱屽姟瀛楀吀淇℃伅
 */
@Data
public class SchoolDuty {

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

    /**
     * 鍒涘缓鏃堕棿锛岃褰曡亴鍔′俊鎭殑娣诲姞鏃堕棿
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;
}
