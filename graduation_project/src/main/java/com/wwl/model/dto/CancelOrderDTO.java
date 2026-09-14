package com.wwl.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CancelOrderDTO {
    /** 订单号 */
    @NotNull(message = "订单id不能为空")
    private String orderNo;

    /** 用户取消原因 */
    @NotBlank(message = "请填写取消原因")
    private String cancelReason;
}