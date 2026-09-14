package com.wwl.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * User address entity.
 * Frontend uses addressNo; internal numeric id/userId are hidden from JSON.
 */
@Data
public class UserAddress {
    /** Internal primary key. */
    @JsonIgnore
    private Long id;

    /** Public address number used by frontend. */
    private String addressNo;

    /** Internal user id. */
    @JsonIgnore
    private Long userId;

    /** Receiver name. */
    private String receiverName;

    /** Receiver phone. */
    private String receiverPhone;

    /** Address type: 1 campus, 2 outside. */
    private Integer addressType;

    /** Full address. */
    private String address;

    /** Default flag: 1 default, 0 normal. */
    private Integer isDefault;

    /** Soft delete flag: 0 active, 1 deleted. */
    private Integer isDeleted;

    /** Created time. */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createdAt;

    /** Updated time. */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime updatedAt;
}