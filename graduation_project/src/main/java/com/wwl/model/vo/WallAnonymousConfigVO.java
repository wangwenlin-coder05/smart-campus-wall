package com.wwl.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * WallAnonymousConfig瑙嗗浘杩斿洖VO绫? */
@Data
public class WallAnonymousConfigVO {
    @JsonIgnore
    private Long id;

    /**
     * 鍖垮悕澶村儚OSS鍦板潃
     */
    private String anonymousAvatar;

    /**
     * 鍖垮悕灞曠ず鏄电О
     */
    private String anonymousNickname;

    /**
     * 鎺掑簭搴忓彿锛屾暟鍊艰秺澶ф帓搴忚秺闈犲墠
     */
    private Integer sortNum;

}
