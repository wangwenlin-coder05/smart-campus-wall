package com.wwl.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * User database entity.
 * This class maps to the user table and is used by auth and profile APIs.
 */
@Data
public class User {
    /** Database primary key. */
    @JsonIgnore
    private Long id;

    /** Public user UID, for example U20260510002. */
    private String uid;

    /** WeChat mini program openid, also used for simulated WeChat login. */
    private String openid;

    /** Phone account. */
    private String phone;

    /** Email account. */
    private String email;

    /** Login password. Plain text is used only during development. */
    private String password;

    /** Display nickname. */
    private String username;

    /** Avatar URL. */
    private String avatar;

    /** Role: 0 normal user, 1 admin. */
    private Integer role;

    /** Real name. */
    private String realName;

    /** Gender: 0 unknown, 1 male, 2 female. */
    private Integer gender;

    /** Identity card number. */
    private String idCard;

    /** Birthday. */
    private LocalDate birthday;

    /** QQ number. */
    private String qq;

    /** WeChat account, also used for simulated WeChat login. */
    private String wechat;

    /** Alipay account, also used for simulated Alipay login. */
    private String alipayAccount;

    /** Student number. */
    private String studentId;

    /** College. */
    private String college;

    /** Major. */
    private String major;

    /** Class name. */
    private String className;

    /** Dormitory. */
    private String dormitory;

    /** Hobby. */
    private String hobby;

    /** Account status: 1 normal, 0 disabled. */
    private Integer status;

    /** Soft delete flag: 0 active, 1 deleted. */
    private Integer isDeleted;

    /** Last login time. */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;

    /** Last login IP. */
    private String lastLoginIp;

    /** Created time. */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /** Updated time. */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}
