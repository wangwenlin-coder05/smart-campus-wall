package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.model.entity.WallSensitiveWord;
import com.wwl.service.IWallSensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 表白墙敏感词控制器
 * 提供敏感词的管理功能
 */
@RestController
@RequestMapping("/wall/sensitive")
public class WallSensitiveWordController {

    @Autowired
    private IWallSensitiveWordService wallSensitiveWordService;

    /**
     * 查询敏感词列表
     * @param word 敏感词关键词（模糊查询，可选）
     * @return 敏感词列表
     */
    @GetMapping("/list")
    public Result list(@RequestParam(required = false) String word){
        List<WallSensitiveWord> list = wallSensitiveWordService.getSensitiveList(word);
        return Result.success(list);
    }

    /**
     * 新增敏感词
     * @param entity 敏感词实体
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody WallSensitiveWord entity){
        boolean res = wallSensitiveWordService.addSensitive(entity);
        return res ? Result.success() : Result.error("新增失败");
    }

    /**
     * 逻辑删除敏感词
     * @param id 敏感词ID
     * @return 操作结果
     */
    @DeleteMapping("/del/{id}")
    public Result del(@PathVariable Long id){
        boolean res = wallSensitiveWordService.delSensitive(id);
        return res ? Result.success() : Result.error("删除失败");
    }
}
