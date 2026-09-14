package com.wwl.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 鏍″洯澧?鍖垮悕閰嶇疆瀹炰綋绫?
 * 瀵瑰簲琛細wall_anonymous_config
 * 鐢ㄤ簬缁熶竴绠＄悊鍖垮悕鍙戝笘銆佸尶鍚嶈瘎璁虹殑澶村儚鍜屾樀绉?
 * @author wwl
 */
public class WallAnonymousConfig {

    /**
     * 涓婚敭鑷ID
     */
    private Long id;

    /**
     * 鍖垮悕澶村儚OSS鍦板潃
     */
    private String anonymousAvatar;

    /**
     * 鍖垮悕灞曠ず鏄电О
     */
    private String anonymousNickname;

    /**
     * 鎺掑簭搴忓彿锛屾暟鍊艰秺澶ф帓搴忚秺闈犲墠
     */
    private Integer sortNum;

    /**
     * 閫昏緫鍒犻櫎鏍囪瘑
     * 0 - 姝ｅ父鏁版嵁
     * 1 - 宸插垹闄ゆ暟鎹?
     */
    private Integer isDeleted;

    /**
     * 鏁版嵁鍒涘缓鏃堕棿
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 鏁版嵁鏇存柊鏃堕棿
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAnonymousAvatar() {
        return anonymousAvatar;
    }

    public void setAnonymousAvatar(String anonymousAvatar) {
        this.anonymousAvatar = anonymousAvatar;
    }

    public String getAnonymousNickname() {
        return anonymousNickname;
    }

    public void setAnonymousNickname(String anonymousNickname) {
        this.anonymousNickname = anonymousNickname;
    }

    public Integer getSortNum() {
        return sortNum;
    }

    public void setSortNum(Integer sortNum) {
        this.sortNum = sortNum;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
