package com.wwl.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单请求参数DTO
 * 接收前端新增、修改、查询订单入参，包含分页、筛选、业务字段
 */
@Data
public class OrderInfoDTO {

    /** 订单主键ID（修改订单时使用） */
    private Long id;

    /** 对外订单编号（时间戳+随机数，隐藏业务主键） */
    private String orderNo;

    /** 下单用户ID */
    private Long userId;

    /** 下单用户UID，前端只传该字段 */
    private String userUid;

    /** 接单骑手ID，未接单时为null */
    private Long riderId;

    /** 一级订单类型：代取/帮买/互助 */
    private String parentType;

    /** 细分类型：快递/奶茶/商超/药品等 */
    private String subType;

    /** 取件地址ID，关联用户地址表 */
    private Long startAddressId;

    /** 取件地址对外编号，前端只传该字段 */
    private String startAddressNo;

    /** 送达地址ID，关联用户地址表 */
    private Long endAddressId;

    /** 送达地址对外编号，前端只传该字段 */
    private String endAddressNo;

    /** 快递取件码 */
    private String fetchCode;

    /** 订单备注说明 */
    private String remark;

    /** 跑腿服务费/赏金 */
    private BigDecimal rewardPrice;

    /** 物品预算金额 */
    private BigDecimal goodsBudget;

    /** 订单总金额 */
    private BigDecimal orderPrice;

    /** 用户期望送达时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime expectTime;

    /** 支付状态 0-未支付 1-已支付 */
    private Integer payStatus;

    /** 订单状态 0-待接单 1-已接单 2-配送中 3-已完成 4-已撤销 */
    private Integer orderStatus;

    /** 订单图片链接集合 */
    private List<String> orderImageList;

    // ==================== 分页&查询专属字段 ====================

    /**
     * 查询场景标识
     * 0-全部订单 1-我发布的 2-我接单的 3-待接单 4-进行中 5-已完成
     */
    private Integer sceneType;

    /** 分页页码 */
    private Integer pageNum;

    /** 每页条数 */
    private Integer pageSize;
}
