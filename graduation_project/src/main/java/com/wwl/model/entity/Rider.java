
package com.wwl.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 骑手数据库实体类
 * 严格对应rider数据表，仅包含数据库真实存在的字段
 */
@Data
public class Rider {
    /** 骑手id */
    private Long id;
    
    /** 骑手uid */
    private String userId;
    
    /** 骑手姓名 */
    private String riderName;
    
    /** 手机号 */
    private String phone;
    
    /** 微信号 */
    private String wechat;
    
    /** 密码 */
    private String password;
    
    /** 状态 1正常 0禁用 */
    private Integer status;
    
    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;
}