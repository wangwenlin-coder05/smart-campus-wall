package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.model.entity.MovieTicket;
import com.wwl.service.TicketService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 浣滆?咃細鐜嬫枃鏋?
 * 鏃堕棿锛?026 2026/5/9 涓嬪崍1:56
 * 鎻忚堪锛?
 */
@RestController
@RequestMapping("/api")
public class TicketController {

    @Resource
    private TicketService ticketService;

    @PostMapping("/upload")
    public Result uploadImg(@RequestParam("file") MultipartFile file) throws Exception {
        MovieTicket ticket = ticketService.uploadAndParseTicket(file);


        return Result.success(ticket);
    }
}
