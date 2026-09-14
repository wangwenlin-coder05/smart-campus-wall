package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.model.entity.SchoolDuty;
import com.wwl.service.SchoolDutyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/duty")
public class SchoolDutyController {

    @Autowired
    private SchoolDutyService dutyService;

    @PostMapping("/add")
    public Result add(@RequestBody SchoolDuty duty){
        return dutyService.addDuty(duty) ? Result.success("新增职务成功") : Result.error("新增失败");
    }

    @GetMapping("/get/{id}")
    public Result get(@PathVariable Integer id){
        return Result.success(dutyService.getById(id));
    }

    @GetMapping("/list")
    public Result list(){
        return Result.success(dutyService.listAll());
    }

    @PostMapping("/search")
    public Result search(@RequestBody SchoolDuty duty){
        return Result.success(dutyService.conditionList(duty));
    }

    @PutMapping("/update")
    public Result update(@RequestBody SchoolDuty duty){
        return dutyService.updateDuty(duty) ? Result.success("修改成功") : Result.error("修改失败");
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id){
        return dutyService.deleteDuty(id) ? Result.success("删除成功") : Result.error("删除失败");
    }
}
