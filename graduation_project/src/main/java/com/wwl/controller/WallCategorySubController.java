package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.model.entity.WallCategorySub;
import com.wwl.service.IWallCategorySubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 表白墙二级分类控制器
 * 提供子分类的增删改查接口
 */
@RestController
@RequestMapping("/wall/sub-category")
public class WallCategorySubController {

    @Autowired
    private IWallCategorySubService wallCategorySubService;

    /**
     * 查询二级分类列表
     * @param categoryId 一级分类ID（可选）
     * @param name 子分类名称（模糊查询，可选）
     * @param isShow 是否展示（0-隐藏 1-展示，可选）
     * @return 子分类列表
     */
    @GetMapping("/list")
    public Result list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer isShow
    ){
        List<WallCategorySub> list = wallCategorySubService.getSubList(categoryId, name, isShow);
        return Result.success(list);
    }

    /**
     * 根据ID查询单个子分类
     * @param id 子分类ID
     * @return 子分类详情
     */
    @GetMapping("/get/{id}")
    public Result get(@PathVariable Long id){
        return Result.success(wallCategorySubService.getSubById(id));
    }

    /**
     * 新增二级分类
     * @param entity 子分类实体
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody WallCategorySub entity){
        boolean res = wallCategorySubService.addSub(entity);
        return res ? Result.success() : Result.error("新增失败");
    }

    /**
     * 修改二级分类
     * @param entity 子分类实体
     * @return 操作结果
     */
    @PutMapping("/edit")
    public Result edit(@RequestBody WallCategorySub entity){
        boolean res = wallCategorySubService.editSub(entity);
        return res ? Result.success() : Result.error("修改失败");
    }

    /**
     * 逻辑删除二级分类
     * @param id 子分类ID
     * @return 操作结果
     */
    @DeleteMapping("/del/{id}")
    public Result del(@PathVariable Long id){
        boolean res = wallCategorySubService.delSub(id);
        return res ? Result.success() : Result.error("删除失败");
    }
}