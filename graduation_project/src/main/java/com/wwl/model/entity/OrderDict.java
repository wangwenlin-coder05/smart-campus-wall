package com.wwl.model.entity;

import lombok.Data;

/**
 * 璁㈠崟鍒嗙被瀛楀吀琛ㄥ疄浣?
 */
@Data
public class OrderDict {
    /** 涓婚敭id */
    private Integer id;
    /** 涓?绾у垎绫伙細浠ｅ彇/甯拱/浜掑姪 */
    private String parentType;
    /** 浜岀骇缁嗗垎鍒嗙被 */
    private String subType;

    /** 鏄惁鍒犻櫎 0-鏈垹闄?1-宸插垹闄?*/
    private Integer isDeleted;
}
