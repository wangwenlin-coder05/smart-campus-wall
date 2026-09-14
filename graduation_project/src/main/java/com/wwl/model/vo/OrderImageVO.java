package com.wwl.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * OrderImage瑙嗗浘杩斿洖VO绫? */
@Data
public class OrderImageVO {
    @JsonIgnore
    private Long id;

    /** 鍏宠仈璁㈠崟id锛屽叧鑱攐rder_info璁㈠崟涓昏〃 */
    private Long orderId;

    /** 闃块噷浜戝浘鐗囧湪绾胯闂摼鎺ュ湴鍧? */
    private String imageUrl;

}
