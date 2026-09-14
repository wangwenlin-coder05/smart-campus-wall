package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.model.entity.Rider;
import com.wwl.service.RiderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 骑手管理控制器
 */
@RestController
@RequestMapping("/rider")
public class RiderController {

    @Autowired
    private RiderService riderService;

    /**
     * 新增骑手
     */
    @PostMapping("/add")
    public Result addRider(@RequestBody Rider rider) {
        boolean success = riderService.addRider(rider);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 根据id查询骑手
     */
    @GetMapping("/{id}")
    public Result getRiderById(@PathVariable("id") Long id) {
        Rider rider = riderService.getRiderById(id);
        return rider != null ? Result.success(rider) : Result.error("骑手不存在");
    }

    /**
     * 根据userId查询骑手
     */
    @GetMapping("/byUserId/{userId}")
    public Result getRiderByUserId(@PathVariable("userId") String userId) {
        Rider rider = riderService.getRiderByUserId(userId);
        return rider != null ? Result.success(rider) : Result.error("骑手不存在");
    }

    /**
     * 查询所有骑手列表
     */
    @GetMapping("/list")
    public Result getRiderList() {
        List<Rider> list = riderService.getRiderList();
        return Result.success(list);
    }

    /**
     * 修改骑手信息
     */
    @PutMapping("/update")
    public Result updateRider(@RequestBody Rider rider) {
        boolean success = riderService.updateRider(rider);
        return success ? Result.success("修改成功") : Result.error("修改失败");
    }

    /**
     * 删除骑手
     */
    @DeleteMapping("/{id}")
    public Result deleteRider(@PathVariable("id") Long id) {
        boolean success = riderService.deleteRider(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 多条件查询骑手列表
     */
    @PostMapping("/query")
    public Result selectRiderListByCondition(@RequestBody(required = false) Rider rider) {
        List<Rider> list = riderService.selectRiderListByCondition(rider);
        return Result.success(list);
    }


}