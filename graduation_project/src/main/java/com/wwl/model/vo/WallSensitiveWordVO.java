package com.wwl.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * WallSensitiveWord瑙嗗浘杩斿洖VO绫? */
@Data
public class WallSensitiveWordVO {
    @JsonIgnore
    private Long id;

    /**
     * 鏁忔劅璇?
     */
    private String word;

}
