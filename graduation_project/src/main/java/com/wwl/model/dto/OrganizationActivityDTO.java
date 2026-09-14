package com.wwl.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrganizationActivityDTO {
    private Long id;
    private Long activityId;
    private String userId;
    private String nickname;
    private String avatar;
    private String keyword;
    private String category;
    private Integer tagId;
    private String tagName;
    private String title;
    private String activityTime;
    private String address;
    private Integer maxPeople;
    private String genderLimit;
    private String feeType;
    private Boolean depositRequired;
    private BigDecimal depositAmount;
    private String hostDesc;
    private String posterImg;
    private String brandName;
    private String brandAvatar;
    private String qrcodeUrl;
    private String status;
}
