package com.wwl.model.dto;

import lombok.Data;

@Data
public class OrderDisputeDTO {
    /** 骑手用户ID */
    private String riderUserId;

    /** 订单号 */
    private String orderNo;

    /** 1 同意取消，2 拒绝取消 */
    private Integer disposeType;

    /** 同意或拒绝取消时填写的原因 */
    private String reason;
}
