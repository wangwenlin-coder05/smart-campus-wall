package com.wwl.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 浣滆?咃細鐜嬫枃鏋?
 * 鏃堕棿锛?026 2026/5/9 涓嬪崍12:36
 * 鎻忚堪锛?
 */
@Data
@Builder
public class MovieTicket {
    private Long id;

    // 褰遍櫌銆佸奖鐗?
    private String cinemaName;
    private String movieName;

    // 鏃堕棿
    private String startDate;
    private String startTime;
    private String endTime;

    // 搴т綅鐩稿叧
    private String seatInfo;
    private Integer seatCount;
    private String seatPrice;

    // 浠锋牸
    private String totalPrice;

    // 鍘熷鏂囨湰
    private String allText;

    // 鍒涘缓鏃堕棿
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;
}




