package com.wwl.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 骑手订单关联实体类
 * 对应数据库表：tb_rider_order
 */
@Data
@Accessors(chain = true) // 开启链式set赋值
public class RiderOrder {

    /**
     * 主键自增ID
     */
    private Long id;

    /**
     * 骑手专属订单编号
     */
    private String riderOrderNo;

    /**
     * 关联平台主订单ID
     */
    private String orderNo;

    /**
     * 接单骑手用户ID
     */
    private String riderUserId;

    /**
     * 骑手确认接单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptTime;

    /**
     * 骑手实际上门取件时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pickTime;

    /**
     * 骑手送达货物确认时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliverTime;

    /**
     * 整个订单流程最终完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    /**
     * 骑手履约状态
     * 0 待取件  1 配送中  2 已完成  3 异常搁置  4 主动弃单
     */
    private Integer riderStatus;

    /**
     * 上一个骑手订单状态
     * 0 待取件  1 配送中  2 已完成  3 异常搁置  4 主动弃单
     */
    private Integer preDeliveryStatus;

    /**
     * 骑手本次订单到手服务佣金
     */
    private BigDecimal serviceFee;

    /**
     * 弃单、取消订单填写的原因备注
     */
    private String cancelReason;

    /**
     * 这条数据入库创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 这条数据最后修改更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识
     * 0 正常数据  1 已逻辑删除
     */
    private Integer isDeleted;
}
