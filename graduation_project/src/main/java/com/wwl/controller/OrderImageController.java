package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.model.entity.OrderImage;
import com.wwl.service.OrderImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/orderImage")
public class OrderImageController {

    @Autowired
    private OrderImageService orderImageService;

    // 新增单张图片
    @PostMapping("/add")
    public Result add(OrderImage orderImage){
        return orderImageService.saveOne(orderImage) ? Result.success("新增成功") : Result.error("新增失败");
    }

    // 批量上传图片
    @PostMapping("/batchAdd")
    public Result batchAdd(@RequestParam("orderId") Long orderId, @RequestBody List<String> urlList){
        boolean res = orderImageService.batchSave(orderId, urlList);
        return res ? Result.success("批量上传成功") : Result.error("上传失败");
    }

    // 查询订单图片
    @GetMapping("/list/{orderId}")
    public Result list(@PathVariable("orderId") Long orderId){
        return Result.success(orderImageService.getListByOrderId(orderId));
    }

    // 修改图片地址
    @PutMapping("/update")
    public Result update(@RequestParam("id") Long id, @RequestParam("imageUrl") String imageUrl){
        return orderImageService.updateImg(id, imageUrl) ? Result.success("修改成功") : Result.error("修改失败");
    }

    // 删除单张
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable("id") Long id){
        return orderImageService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }

    // 删除订单全部图片
    @DeleteMapping("/clear/{orderId}")
    public Result clear(@PathVariable("orderId") Long orderId){
        return orderImageService.removeByOrderId(orderId) ? Result.success("清空成功") : Result.error("清空失败");
    }
}
