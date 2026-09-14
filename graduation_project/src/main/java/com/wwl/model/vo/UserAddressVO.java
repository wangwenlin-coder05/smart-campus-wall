package com.wwl.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * UserAddress瑙嗗浘杩斿洖VO绫? */
@Data
public class UserAddressVO {
    @JsonIgnore
    private Long id;

    /** 鍏宠仈鐢ㄦ埛id */
    private Long userId;

    /** 鏀惰揣浜哄鍚?*/
    private String receiverName;

    /** 鏀惰揣浜烘墜鏈哄彿 */
    private String receiverPhone;

    /** 鍦板潃绫诲瀷 1鏍″洯 2鏍″ */
    private Integer addressType;

    /** 瀹屾暣鍦板潃锛堟牎鍖?妤兼爧+妤煎眰+闂ㄧ墝鍙凤級 */
    private String address;

    /** 鏄惁榛樿鍦板潃 1榛樿 0鏅??*/
    private Integer isDefault;

}
