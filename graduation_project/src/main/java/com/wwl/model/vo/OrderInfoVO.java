package com.wwl.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单视图返回VO
 * 用于后端返回前端页面展示
 * 额外增加联表查询的地址文本字段，用于页面直接展示
 */
@Data
public class OrderInfoVO {
    /** 订单主键id（仅内部使用，不返回给前端） */
    @JsonIgnore
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

    /** 取件完整地址文本（联表查询） */
    private String startAddressText;

    /** 送达完整地址文本（联表查询） */
    private String endAddressText;

    /** 快递取件码 */
    private String fetchCode;

    /** 订单备注说明 */
    private String remark;

    /** 小费金额 */
    private BigDecimal rewardPrice;

    /** 帮买物品预算金额 */
    private BigDecimal goodsBudget;

    /** 订单总费用 */
    private BigDecimal orderPrice;

    /** 用户期望送达时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime expectTime;

    /** 支付状态 0未支付 1已支付 */
    private Integer payStatus;

    /** 订单状态 0待接单 1已接单 2配送中 3已完成 4已撤销 */
    private Integer orderStatus;

    /** 订单创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;

    /** 订单更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /** 订单图片链接列表 */
    private List<String> orderImageList;

    /** 骑手接单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime acceptTime;

    /** 骑手取件时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime pickTime;

    /** 骑手送达时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime deliverTime;

    /** 订单完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime finishTime;

    /** 骑手服务费 */
    private BigDecimal serviceFee;

    /** 取消原因 */
    private String cancelReason;

    /** 客户已申请取消次数 */
    private Integer cancelApplyCount;

    /** 订单生命流程日志 */
    private String lifecycleLog;

    /** 骑手取消/拒绝取消原因 */
    private String riderCancelReason;
}
