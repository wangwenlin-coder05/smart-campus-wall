package com.wwl.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LostFoundItem {
    private Long id;
    private String type;
    private String title;
    private String description;
    private String location;
    private String image;
    private String publisherUid;
    private String publisherName;
    private String publisherPhone;
    private String publisherStudentId;
    private String publisherAvatar;

    private Integer status;
    private Integer views;
    private Long resolvedApplicationId;

    private Long applicationId;
    private String applicationStatus;
    private String applyType;
    private String applicantUid;
    private String applicantName;
    private String applicantPhone;
    private String applicantStudentId;
    private String applicantIdCard;
    private String applicantAvatar;

    private String returnerName;
    private String claimerName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime returnTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime claimTime;

    private Integer isDeleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime updateTime;
}