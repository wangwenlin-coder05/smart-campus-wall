package com.wwl.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 璁㈠崟鍥剧墖闄勮〃瀹炰綋绫?
 * 瀛樻斁璁㈠崟涓婁紶鐨勬墍鏈夊浘鐗囬摼鎺ワ紝鍜岃鍗曚富琛ㄤ竴瀵瑰鍏宠仈
 */
@Data
public class OrderImage {

    /** 涓婚敭鑷id */
    private Long id;

    /** 鍏宠仈璁㈠崟id锛屽叧鑱攐rder_info璁㈠崟涓昏〃 */
    private Long orderId;

    /** 闃块噷浜戝浘鐗囧湪绾胯闂摼鎺ュ湴鍧? */
    private String imageUrl;

    /** 鏄惁鍒犻櫎 0-鏈垹闄?1-宸插垹闄?*/
    private Integer isDeleted;

    /** 鍥剧墖涓婁紶淇濆瓨鏃堕棿 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createdAt;
}

