package com.wwl.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrganizationActivity {
    private Long id;
    private String title;
    private String category;
    private Integer tagId;
    private String tagName;
    private String activityTime;
    private String address;
    private Integer maxPeople;
    private Integer joinNum;
    private String genderLimit;
    private String feeType;
    private Boolean depositRequired;
    private BigDecimal depositAmount;
    private String hostDesc;
    private String posterImg;
    private String brandName;
    private String brandAvatar;
    private Integer groupCount;
    private String qrcodeUrl;
    private String creatorUserId;
    private String status;
    private Integer isDeleted;
    private List<String> avatarList;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
