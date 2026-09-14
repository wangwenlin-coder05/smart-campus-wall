package com.wwl.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatusEnum {
    WAIT_ACCEPT(0, "待接单"),
    ACCEPTED(1, "已接单"),
    DELIVERING(2, "配送中"),
    ARRIVED(3, "已送达"),
    DISPUTE(4, "取消申请中"),
    FINISHED(5, "已完成"),
    CANCELLED(6, "已取消");

    private final Integer code;
    private final String desc;

    public static String getDescByCode(Integer code) {
        for (OrderStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status.getDesc();
            }
        }
        return "未知状态";
    }
}
