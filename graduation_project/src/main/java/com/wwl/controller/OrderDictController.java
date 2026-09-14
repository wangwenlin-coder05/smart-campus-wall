package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.model.entity.OrderDict;
import com.wwl.service.OrderDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单分类字典控制器
 */
@RestController
@RequestMapping("/orderDict")
public class OrderDictController {

    @Autowired
    private OrderDictService orderDictService;

    /** 新增分类 */
    @PostMapping("/add")
    public Result add(@RequestBody OrderDict dict){
        return orderDictService.addDict(dict) ? Result.success("分类新增成功") : Result.error("新增失败");
    }

    /** 根据id查询 */
    @GetMapping("/get/{id}")
    public Result get(@PathVariable("id") Integer id){
        return Result.success(orderDictService.getById(id));
    }

    /** 查询全部分类（前端下拉） */
    @GetMapping("/list")
    public Result list(){
        return Result.success(orderDictService.getAllList());
    }

    /** 多条件查询 */
    @PostMapping("/search")
    public Result search(@RequestBody OrderDict dict){
        return Result.success(orderDictService.conditionList(dict));
    }

    /** 修改分类 */
    @PutMapping("/update")
    public Result update(@RequestBody OrderDict dict){
        return orderDictService.updateDict(dict) ? Result.success("修改成功") : Result.error("修改失败");
    }

    /** 删除分类 */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable("id") Integer id){
        return orderDictService.deleteDict(id) ? Result.success("删除成功") : Result.error("删除失败");
    }
}
