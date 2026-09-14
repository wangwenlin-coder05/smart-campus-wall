package com.wwl.model.entity;

import lombok.Data;

/**
 * 璁㈠崟鐖跺瓙鍒嗙被瀹炰綋绫?
 * 妯′豢鐪佸競鍘夸笂涓嬬骇鑱斿姩缁撴瀯
 */
@Data
public class OrderCategory {

    /** 鍒嗙被涓婚敭id */
    private Integer id;

    /** 鍒嗙被鍚嶇О */
    private String catName;

    /**
     * 鐖剁骇鍒嗙被id
     * parentId = 0 浠ｈ〃涓?绾ч《绾у垎绫?
     * parentId = 瀵瑰簲涓?绾d 浠ｈ〃浜岀骇瀛愬垎绫?
     */
    private Integer parentId;

    /** 鏄惁鍒犻櫎 0-鏈垹闄?1-宸插垹闄?*/
    private Integer isDeleted;
}
