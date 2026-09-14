package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.model.entity.WallCategory;
import com.wwl.service.IWallCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 表白墙一级分类控制器
 * 提供分类的增删改查接口
 */
@RestController
@RequestMapping("/wall/category")
public class WallCategoryController {

    @Autowired
    private IWallCategoryService wallCategoryService;

    /**
     * 查询一级分类列表
     * @param name 分类名称（模糊查询，可选）
     * @param isShow 是否展示（0-隐藏 1-展示，可选）
     * @return 分类列表
     */
    @GetMapping("/list")
    public Result list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer isShow
    ){
        List<WallCategory> list = wallCategoryService.getCategoryList(name, isShow);
        return Result.success(list);
    }

    /**
     * 根据ID查询单个分类
     * @param id 分类ID
     * @return 分类详情
     */
    @GetMapping("/get/{id}")
    public Result get(@PathVariable Long id){
        return Result.success(wallCategoryService.getCategoryById(id));
    }

    /**
     * 新增一级分类
     * @param entity 分类实体
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody WallCategory entity){
        boolean res = wallCategoryService.addCategory(entity);
        return res ? Result.success() : Result.error("新增失败");
    }

    /**
     * 修改一级分类
     * @param entity 分类实体
     * @return 操作结果
     */
    @PutMapping("/edit")
    public Result edit(@RequestBody WallCategory entity){
        boolean res = wallCategoryService.editCategory(entity);
        return res ? Result.success() : Result.error("修改失败");
    }

    /**
     * 逻辑删除一级分类
     * @param id 分类ID
     * @return 操作结果
     */
    @DeleteMapping("/del/{id}")
    public Result del(@PathVariable Long id){
        boolean res = wallCategoryService.delCategory(id);
        return res ? Result.success() : Result.error("删除失败");
    }
}