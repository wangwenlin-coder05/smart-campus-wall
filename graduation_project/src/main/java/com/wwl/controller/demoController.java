package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.model.dto.OrderInfoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 作者：王文林
 * 时间：2026 2026/6/1 上午10:19
 * 描述：
 */

    @Tag(name = "投票管理")
    @RestController
    @RequestMapping("/vote")
    public class demoController {

        @Operation(summary = "给作品投票", description = "用户对指定作品进行投票")
        @GetMapping("/doVote")
        public Result doVote(OrderInfoDTO orderInfoDTO){
            // 业务代码还没写
            return Result.success();
        }
    }