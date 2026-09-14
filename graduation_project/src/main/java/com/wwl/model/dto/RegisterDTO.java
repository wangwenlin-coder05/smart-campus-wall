package com.wwl.model.dto;

import lombok.Data;

/**
 * 注册请求参数。
 */
@Data
public class RegisterDTO {
    /** 注册方式：phone/email/wechat/alipay */
    private String registerType;

    /** 注册账号：手机号、邮箱、微信号或支付宝账号 */
    private String account;

    /** 密码 */
    private String password;

    /** 昵称 */
    private String username;

    /** 头像地址，可不传，后端会给默认头像 */
    private String avatar;

    /** 性别：0未知，1男，2女 */
    private Integer gender;

    /** 模拟验证码：开发阶段固定 123456 */
    private String code;
}
