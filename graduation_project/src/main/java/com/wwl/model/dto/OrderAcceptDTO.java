package com.wwl.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderAcceptDTO {
    /** 主订单号 */
    private String orderNo;

    /** 骑手用户ID */
    private String riderUserId;

    /** 服务佣金 */
    private BigDecimal serviceFee;

    /** 订单状态 */
    private Integer orderStatus;

    /** 骑手主动取消订单原因 */
    private String cancelReason;
}
