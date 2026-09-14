package com.wwl.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * UserAddress请求参数DTO类
 */
@Data
public class UserAddressDTO {
    /** 主键id */
    private Long id;

    /** 关联用户id */
    private Long userId;

    /** 收货人姓名 */
    private String receiverName;

    /** 收货人手机号 */
    private String receiverPhone;

    /** 地址类型 1校园 2校外 */
    private Integer addressType;

    /** 完整地址（校区+楼栋+楼层+门牌号） */
    private String address;

    /** 是否默认地址 1默认 0普通 */
    private Integer isDefault;

    /** 是否删除 0-未删除 1-已删除 */
    private Integer isDeleted;


    // ==================== 查询专用字段 ====================
    private Integer sceneType;
    private Integer pageNum;
    private Integer pageSize;
}
