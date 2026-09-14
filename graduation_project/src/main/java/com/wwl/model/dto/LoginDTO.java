package com.wwl.model.dto;

import lombok.Data;

/**
 * 登录请求参数。
 */
@Data
public class LoginDTO {
    /** 登录方式：phone/email/wechat/alipay */
    private String loginType;

    /** 登录账号：手机号、邮箱、微信号或支付宝账号 */
    private String account;

    /** 密码 */
    private String password;

    /** 模拟验证码：开发阶段固定 123456 */
    private String code;
}
