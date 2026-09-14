package com.wwl.model.vo;

import com.wwl.model.entity.User;
import lombok.Data;

/**
 * 登录/注册成功后返回给前端的数据。
 */
@Data
public class AuthVO {
    /** 开发阶段使用的简单令牌，前端缓存即可 */
    private String token;

    /** 当前登录用户信息 */
    private User user;
}
