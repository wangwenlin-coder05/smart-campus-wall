package com.wwl.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wwl.model.entity.WallComment;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

/**
 * WallPost瑙嗗浘杩斿洖VO绫?
 */
@Data
public class WallPostVO {
    @JsonIgnore
    private Long id;

    /**
     * 鍙戝竷鐢ㄦ埛UID锛堝叧鑱旀煡璇㈣繑鍥烇級
     */
    private String userUid;

    /**
     * 涓?绾у垎绫籌D
     */
    private Long categoryId;

    /**
     * 浜岀骇瀛愬垎绫籌D
     */
    private Long subCategoryId;

    /**
     * 鏄电О
     */
    private String nickname;

    /**
     * 澶村儚鍦板潃
     */
    private String avatar;

    /**
     * 0瀹炲悕 1鍖垮悕
     */
    private Integer isAnonymous;

    /**
     * 甯栧瓙鏂囧瓧鍐呭
     */
    private String content;

    /**
     * 娴忚閲?
     */
    private Integer viewCount;

    /**
     * 甯栧瓙鐐硅禐鏁?
     */
    private Integer likeCount;

    /**
     * 鏀惰棌鏁?
     */
    private Integer collectCount;

    /**
     * 璇勮鎬绘暟
     */
    private Integer commentCount;

    /**
     * 0寰呭鏍?1瀹℃牳閫氳繃 2椹冲洖
     */
    private Integer status;

    /**
     * 鏄惁缃《 0鍚?1鏄?
     */
    private Integer isTop;

    /**
     * 0绔嬪嵆鍙戝竷 1瀹氭椂鍙戝竷
     */
    private Integer isTiming;

    /**
     * 鍥剧墖/瑙嗛/闊抽娣峰悎璧勬簮鍦板潃
     * 鍓嶇涓婁紶瀹屾嫾鎺ラ?楀彿鍒嗛殧瀛楃涓诧紝鏈?澶?涓?
     */
    private List<String> mediaUrlList;

    /**
     * 鐑棬璇勮鍒楄〃锛堢偣璧炴暟鏈?楂樼殑鍓?鏉★級
     * 闈炴暟鎹簱瀛楁锛屼粎鐢ㄤ簬杩斿洖鍓嶇
     */
    private List<WallComment> hotComments;

}
