package com.wwl.model.dto;

import lombok.Data;

@Data
public class LostFoundDTO {
    private Long id;
    private Long itemId;
    private Long applicationId;
    private String type;
    private String applyType;
    private String keyword;
    private String status;
    private String userUid;
    private String name;
    private String phone;
    private String studentId;
    private String idCard;
    private String title;
    private String description;
    private String location;
    private String image;

    private Integer pageNum;
    private Integer pageSize;
}
