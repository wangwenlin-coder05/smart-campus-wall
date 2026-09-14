package com.wwl.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * UserDutyRel瑙嗗浘杩斿洖VO绫? */
@Data
public class UserDutyRelVO {
    @JsonIgnore
    private Long id;

    /** 鐢ㄦ埛id */
    private Long userId;

    /** 鑱屽姟id 瀵瑰簲 school_duty 琛╥d */
    private Integer dutyId;

}
