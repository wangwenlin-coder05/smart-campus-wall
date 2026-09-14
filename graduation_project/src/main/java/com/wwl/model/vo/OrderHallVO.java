package com.wwl.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 骑手接单大厅列表VO
 * 展示简略起止地址，隐藏所有隐私敏感字段
 */
@Data
public class OrderHallVO {
    /**
     * 订单号
     */
    private String orderNo;
    /**
     * 一级类型：代取/帮买/互助
     */
    private String parentType;

    /**
     * 细分类型
     */
    private String subType;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 帮买物品预算
     */
    private BigDecimal goodsBudget;

    /**
     * 跑腿赏金/小费
     */
    private BigDecimal rewardPrice;

    /**
     * 订单费用
     */
    private BigDecimal orderPrice;

    /**
     * 期望送达时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime expectTime;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 下单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    // 新增：大厅展示简略起止地址
    /** 出发简略地址 */
    private String startAddress;
    /** 目的简略地址 */
    private String endAddress;
}
