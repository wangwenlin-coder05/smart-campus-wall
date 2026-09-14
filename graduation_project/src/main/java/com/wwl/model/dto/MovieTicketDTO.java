package com.wwl.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * MovieTicket璇锋眰鍙傛暟DTO绫? */
@Data
public class MovieTicketDTO {

    // ==================== 鏌ヨ涓撶敤瀛楁 ====================
    private Integer sceneType;
    private Integer pageNum;
    private Integer pageSize;
}
