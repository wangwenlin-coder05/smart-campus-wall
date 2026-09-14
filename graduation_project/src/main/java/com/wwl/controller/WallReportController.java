package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.model.entity.WallReport;
import com.wwl.service.IWallReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 表白墙举报控制器
 * 提供帖子和评论的举报功能
 */
@RestController
@RequestMapping("/wall/report")
public class WallReportController {

    @Autowired
    private IWallReportService wallReportService;

    /**
     * 查询举报记录列表
     * @param postId 帖子ID（可选）
     * @param commentId 评论ID（可选）
     * @param status 处理状态（0-待处理 1-已处理 2-已忽略，可选）
     * @return 举报记录列表
     */
    @GetMapping("/list")
    public Result list(
            @RequestParam(required = false) Long postId,
            @RequestParam(required = false) Long commentId,
            @RequestParam(required = false) Integer status
    ){
        List<WallReport> list = wallReportService.getReportList(postId,commentId,status);
        return Result.success(list);
    }

    /**
     * 提交举报
     * @param entity 举报实体
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody WallReport entity){
        boolean res = wallReportService.addReport(entity);
        return res ? Result.success() : Result.error("举报提交失败");
    }

    /**
     * 处理举报（修改状态）
     * @param entity 举报实体
     * @return 操作结果
     */
    @PutMapping("/edit")
    public Result edit(@RequestBody WallReport entity){
        boolean res = wallReportService.editReport(entity);
        return res ? Result.success() : Result.error("处理失败");
    }

    /**
     * 逻辑删除举报记录
     * @param id 举报ID
     * @return 操作结果
     */
    @DeleteMapping("/del/{id}")
    public Result del(@PathVariable Long id){
        boolean res = wallReportService.delReport(id);
        return res ? Result.success() : Result.error("删除失败");
    }
}
