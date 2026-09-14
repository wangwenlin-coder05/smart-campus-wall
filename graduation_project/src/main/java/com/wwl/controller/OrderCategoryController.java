package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.model.entity.OrderCategory;
import com.wwl.service.OrderCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 订单分类控制器
 * 提供一级二级下拉联动接口
 */
@RestController
@RequestMapping("/category")
public class OrderCategoryController {

    @Autowired
    private OrderCategoryService orderCategoryService;

    /**
     * 获取一级分类接口
     * 前端首页下拉加载大类：代取服务、帮买服务、跑腿代办
     */
    @GetMapping("/first")
    public Result getFirstList(){
        List<OrderCategory> list = orderCategoryService.getFirstCategoryList();
        return Result.success(list);
    }

    /**
     * 根据一级id获取二级子分类
     * 选中一级后，自动加载对应的子类
     */
    @GetMapping("/child/{parentId}")
    public Result getChildList(@PathVariable("parentId") Integer parentId){
        List<OrderCategory> list = orderCategoryService.getChildCategoryList(parentId);
        return Result.success(list);
    }

    /**
     * 后台新增分类
     */
    @PostMapping("/add")
    public Result add(@RequestBody OrderCategory category){
        boolean flag = orderCategoryService.addCategory(category);
        return flag ? Result.success("分类新增成功") : Result.error("新增失败");
    }

    /**
     * 后台修改分类
     */
    @PutMapping("/update")
    public Result update(@RequestBody OrderCategory category){
        boolean flag = orderCategoryService.editCategory(category);
        return flag ? Result.success("分类修改成功") : Result.error("修改失败");
    }

    /**
     * 后台删除分类
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable("id") Integer id){
        boolean flag = orderCategoryService.removeCategory(id);
        return flag ? Result.success("删除成功") : Result.error("删除失败");
    }
}