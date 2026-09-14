package com.wwl.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表数据库实体类
 * 严格对应order_info数据表，仅包含数据库真实存在字段
 * 用于MyBatis增删改查操作，不做任何页面展示用途
 */
@Data
public class OrderInfo {
    /** 订单主键id */
    private Long id;

    /** 对外订单编号（时间戳+随机数，不暴露业务量） */
    private String orderNo;

    /** 下单用户id */
    private Long userId;

    /** 接单骑手id，未接单为null */
    private Long riderId;

    /** 一级订单类型：代取/帮买/互助 */
    private String parentType;

    /** 细分类型：快递/奶茶/商超/药品等 */
    private String subType;

    /** 取件地址id，关联用户地址表 */
    private Long startAddressId;

    /** 送达地址id，关联用户地址表 */
    private Long endAddressId;

    /** 快递取件码 */
    private String fetchCode;

    /** 订单备注说明 */
    private String remark;

    /** 跑腿赏金 */
    private BigDecimal rewardPrice;

    /** 帮买物品预算金额 */
    private BigDecimal goodsBudget;

    /** 订单总金额 */
    private BigDecimal orderPrice;

    /** 用户期望送达时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime expectTime;

    /** 支付状态 0未支付 1已支付 */
    private Integer payStatus;

    /** 订单状态 0待接单 1已接单 2配送中 3已完成 4已撤销 */
    private Integer orderStatus;

    /** 用户取消原因 */
    @NotBlank(message = "请填写取消原因")
    private String cancelReason;

    /** 客户已申请取消次数 */
    private Integer cancelApplyCount;

    /** 订单生命流程日志 */
    private String lifecycleLog;

    /** 上一个订单状态 0-待接单 1-已接单 2-配送中 3-已完成 4-已撤销 */
    private Integer preOrderStatus;

    /** 是否删除 0-未删除 1-已删除 */
    private Integer isDeleted;

    /** 订单创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;

    /** 订单更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
