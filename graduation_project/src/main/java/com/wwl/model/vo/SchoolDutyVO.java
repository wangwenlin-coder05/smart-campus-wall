package com.wwl.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * SchoolDuty瑙嗗浘杩斿洖VO绫? */
@Data
public class SchoolDutyVO {
    @JsonIgnore
    private Long id;

    /**
     * 鑱屽姟鍚嶇О锛屽锛氱彮闀裤?佸洟鏀功銆佸鐢熶細骞蹭簨绛?
     */
    private String dutyName;

    /**
     * 鑱屽姟璇存槑锛屾弿杩拌亴鍔＄殑鑱岃矗鍜屽伐浣滃唴瀹?
     */
    private String dutyDesc;

}
