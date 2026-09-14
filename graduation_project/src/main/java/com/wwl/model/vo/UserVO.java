package com.wwl.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 鐢ㄦ埛瑙嗗浘杩斿洖VO绫? * 鐢ㄤ簬鍚庣杩斿洖鍓嶇椤甸潰灞曠ず
 */
@Data
public class UserVO {
    /** 涓婚敭id锛堜粎鍐呴儴浣跨敤锛屼笉杩斿洖缁欏墠绔級 */
    @JsonIgnore
    private Long id;
    
    /** 澶氱缁熶竴鍞竴id */
    private String uid;
    
    /** 寰俊灏忕▼搴忓敮涓?鏍囪瘑 */
    private String openid;
    
    /** 鎵嬫満鍙凤紝鐧诲綍璐﹀彿 */
    private String phone;
    
    /** 鐢ㄦ埛鏄电О */
    private String username;
    
    /** 澶村儚鍦板潃 */
    private String avatar;
    
    /** 瑙掕壊 0鏅?氱敤鎴?1绠＄悊鍛?*/
    private Integer role;
    
    /** 鐪熷疄濮撳悕 */
    private String realName;
    
    /** 鎬у埆 0鏈煡 1鐢?2濂?*/
    private Integer gender;
    
    /** 韬唤璇佸彿 */
    private String idCard;
    
    /** 鐢熸棩 */
    private LocalDate birthday;
    
    /** QQ鍙?*/
    private String qq;
    
    /** 寰俊鍙?*/
    private String wechat;
    
    /** 瀛﹀彿 */
    private String studentId;
    
    /** 鎵?灞炲闄?*/
    private String college;
    
    /** 鎵?灞炰笓涓?*/
    private String major;
    
    /** 鐝骇 */
    private String className;
    
    /** 瀹胯垗瀵濆 */
    private String dormitory;
    
    /** 涓汉鐖卞ソ */
    private String hobby;
    
    /** 璐﹀彿鐘舵??1姝ｅ父 0绂佺敤 */
    private Integer status;
    
    /** 鏈?鍚庣櫥褰曟椂闂?*/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;
    
    /** 鏈?鍚庣櫥褰旾P */
    private String lastLoginIp;
    
    /** 鏁版嵁鍒涘缓鏃堕棿 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createdAt;
}
