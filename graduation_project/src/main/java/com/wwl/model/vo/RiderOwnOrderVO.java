package com.wwl.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RiderOwnOrderVO {
    // 主订单信息
    @JsonIgnore
    private Long id;
    private String orderNo;
//    private Long userId;
    // 修正
    private String parentType;
    private String subType;

    private BigDecimal orderPrice;
    private BigDecimal rewardPrice;
    private BigDecimal goodsBudget;
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime expectTime;
    private Integer orderStatus;
    private String cancelReason;
    private String riderCancelReason;
    private String statusName;
    private Integer payStatus;
    private String fetchCode;

    /**
     * 订单图片链接集合
     */
    private List<String> orderImageList;

    private LocalDateTime createTime;

    // 地址
    private String startAddressText;
    private String endAddressText;

    // 骑手接单附属信息
    private String riderOrderNo;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime acceptTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime pickTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime deliverTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime finishTime;
    private Integer riderStatus;
    private BigDecimal serviceFee;

}
