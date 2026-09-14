package com.wwl.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LostFoundApplication {
    private Long id;
    private Long itemId;
    private String applyType;
    private String applicantUid;
    private String applicantName;
    private String applicantPhone;
    private String applicantStudentId;
    private String applicantIdCard;
    private String status;
    private Integer isDeleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
