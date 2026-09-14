package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.mapper.UserMapper;
import com.wwl.model.dto.LoginDTO;
import com.wwl.model.dto.RegisterDTO;
import com.wwl.model.entity.User;
import com.wwl.model.vo.AuthVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

/**
 * 开发阶段登录注册控制器。
 * 当前版本只做基础账号能力，token 用于前端缓存身份，不做频繁校验。
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String DEFAULT_AVATAR =
            "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png";

    @Autowired
    private UserMapper userMapper;

    /**
     * 登录：支持手机号、邮箱、微信、支付宝。
     * 没有短信服务时，可以用密码登录，也可以传模拟验证码 123456。
     */
    @PostMapping("/login")
    public Result<AuthVO> login(@RequestBody LoginDTO dto) {
        String loginType = normalizeType(dto.getLoginType());
        String account = normalizeAccount(dto.getAccount());
        if (account.isBlank()) {
            return Result.error("请输入登录账号");
        }

        User user = userMapper.selectUserByAccount(loginType, account);
        if (user == null || user.getIsDeleted() != 0) {
            return Result.error("账号不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }
        boolean passwordOk = dto.getPassword() != null && dto.getPassword().equals(user.getPassword());
        boolean codeOk = "123456".equals(dto.getCode());
        if (!passwordOk && !codeOk) {
            return Result.error("密码或验证码错误");
        }

        return Result.success(buildAuthVO(user));
    }

    /**
     * 注册：支持手机号、邮箱、微信、支付宝。
     * 当前没有短信/三方授权服务，验证码固定模拟为 123456。
     */
    @PostMapping("/register")
    public Result<AuthVO> register(@RequestBody RegisterDTO dto) {
        String registerType = normalizeType(dto.getRegisterType());
        String account = normalizeAccount(dto.getAccount());

        if (!isAccountValid(registerType, account)) {
            return Result.error("账号格式不正确");
        }
        if (!"123456".equals(dto.getCode())) {
            return Result.error("验证码错误，开发阶段请使用123456");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            return Result.error("密码至少6位");
        }
        if (userMapper.selectUserByAccount(registerType, account) != null) {
            return Result.error("账号已注册");
        }

        User user = new User();
        user.setUid(generateUid());
        fillRegisterAccount(user, registerType, account);
        user.setPassword(dto.getPassword());
        user.setUsername(isBlank(dto.getUsername()) ? "校园用户" : dto.getUsername());
        user.setAvatar(isBlank(dto.getAvatar()) ? DEFAULT_AVATAR : dto.getAvatar());
        user.setRole(0);
        user.setGender(dto.getGender() == null ? 0 : dto.getGender());
        user.setStatus(1);
        user.setIsDeleted(0);
        userMapper.insertUser(user);

        User savedUser = userMapper.selectUserByAccount(registerType, account);
        return Result.success(buildAuthVO(savedUser));
    }

    /**
     * 根据 uid 获取当前用户信息。
     * 这里不强依赖 token，方便开发阶段调试。
     */
    @GetMapping("/me")
    public Result<User> me(@RequestParam("uid") String uid) {
        User user = userMapper.selectUserByUid(uid);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    private AuthVO buildAuthVO(User user) {
        user.setPassword(null);
        AuthVO vo = new AuthVO();
        vo.setToken(createDevToken(user.getUid()));
        vo.setUser(user);
        return vo;
    }

    /**
     * 生成对外用户UID：U + 日期时间 + 6位随机字符。
     */
    private String generateUid() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "U" + time + random;
    }

    /**
     * 开发阶段简单令牌：只用于前端记住登录态，不做高频服务端验证。
     */
    private String createDevToken(String uid) {
        String raw = uid + ":" + System.currentTimeMillis();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            return "phone";
        }
        return type.trim().toLowerCase();
    }

    private String normalizeAccount(String account) {
        return account == null ? "" : account.trim();
    }

    private boolean isAccountValid(String type, String account) {
        if (account.isBlank()) {
            return false;
        }
        return switch (type) {
            case "phone" -> account.matches("^1\\d{10}$");
            case "email" -> account.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
            case "wechat", "alipay" -> account.length() >= 3 && account.length() <= 100;
            default -> false;
        };
    }

    /**
     * 根据注册方式，把账号写入对应字段。
     */
    private void fillRegisterAccount(User user, String type, String account) {
        switch (type) {
            case "phone" -> user.setPhone(account);
            case "email" -> user.setEmail(account);
            case "wechat" -> user.setWechat(account);
            case "alipay" -> user.setAlipayAccount(account);
            default -> user.setPhone(account);
        }
    }
}
