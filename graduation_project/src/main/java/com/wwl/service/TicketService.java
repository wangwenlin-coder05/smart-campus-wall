package com.wwl.service;

import com.wwl.model.entity.MovieTicket;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 浣滆?咃細鐜嬫枃鏋?
 * 鏃堕棿锛?026 2026/5/9 涓嬪崍1:57
 * 鎻忚堪锛?
 */

public interface TicketService {
    MovieTicket uploadAndParseTicket(MultipartFile file) throws IOException;
}
