package com.wwl.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户请求参数DTO类
 * 用于接收前端查询、提交、修改用户的入参
 */
@Data
public class UserDTO {
    /** 主键id */
    private Long id;
    
    /** 多端统一唯一id */
    private String uid;
    
    /** 微信小程序唯一标识 */
    private String openid;
    
    /** 手机号，登录账号 */
    private String phone;
    
    /** 登录密码 */
    private String password;
    
    /** 用户昵称 */
    private String username;
    
    /** 头像地址 */
    private String avatar;
    
    /** 角色 0普通用户 1管理员 */
    private Integer role;
    
    /** 真实姓名 */
    private String realName;
    
    /** 性别 0未知 1男 2女 */
    private Integer gender;
    
    /** 身份证号 */
    private String idCard;
    
    /** 生日 */
    private LocalDate birthday;
    
    /** QQ号 */
    private String qq;
    
    /** 微信号 */
    private String wechat;
    
    /** 学号 */
    private String studentId;
    
    /** 所属学院 */
    private String college;
    
    /** 所属专业 */
    private String major;
    
    /** 班级 */
    private String className;
    
    /** 宿舍寝室 */
    private String dormitory;
    
    /** 个人爱好 */
    private String hobby;
    
    /** 账号状态 1正常 0禁用 */
    private Integer status;

    // ==================== 查询专用字段 ====================
    /** 查询场景标识 */
    private Integer sceneType;
    /** 分页页码 */
    private Integer pageNum;
    /** 每页条数 */
    private Integer pageSize;
}
