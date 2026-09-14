package com.wwl.model.entity;

import lombok.Data;

/**
 * Friend relation entity.
 */
@Data
public class UserFriend {
    private Long id;
    private String userUid;
    private String friendUid;
    private String remark;
    private Integer status;
}
