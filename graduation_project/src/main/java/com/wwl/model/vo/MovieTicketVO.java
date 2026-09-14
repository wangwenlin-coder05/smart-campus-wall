package com.wwl.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * MovieTicket瑙嗗浘杩斿洖VO绫? */
@Data
public class MovieTicketVO {
    @JsonIgnore
    private Long id;

}
