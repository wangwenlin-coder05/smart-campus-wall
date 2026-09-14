package com.wwl.model.dto;

import lombok.Data;

/**
 * WallAnonymousConfig请求参数DTO */
@Data
public class WallAnonymousConfigDTO {
    /**
     * 主键自增ID
     */
    private Long id;

    /**
     * 匿名头像OSS地址
     */
    private String anonymousAvatar;

    /**
     * 匿名展示昵称
     */
    private String anonymousNickname;

    /**
     * 排序序号，数值越大排序越靠前
     */
    private Integer sortNum;

    /**
     * 逻辑删除标识
     * 0 - 正常数据
     * 1 - 已删除数据
     */
    private Integer isDeleted;


    // ==================== 查询专用字段 ====================
    private Integer sceneType;
    private Integer pageNum;
    private Integer pageSize;
}