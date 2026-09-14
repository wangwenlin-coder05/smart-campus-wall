package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.service.UserDutyRelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 用户职务关联接口
 * 提供：查询用户职务、保存勾选职务
 */
@RestController
@RequestMapping("/userDuty")
public class UserDutyRelController {

    @Autowired
    private UserDutyRelService userDutyRelService;

    /**
     * 获取用户已选职务id列表（回显勾选）
     */
    @GetMapping("/list/{userId}")
    public Result getDutyList(@PathVariable Long userId){
        List<Integer> list = userDutyRelService.getUserDutyList(userId);
        return Result.success(list);
    }

    /**
     * 获取用户已选职务名称列表（回显勾选）
     */
    @GetMapping("/nameList/{userId}")
    public Result getDutyNameList(@PathVariable Long userId){
        List<String> nameList = userDutyRelService.getUserDutyNameList(userId);
        return Result.success(nameList);
    }


    /**
     * 保存用户勾选的多个职务
     * 前端传：用户id + 勾选的职务id数组
     */
    @PostMapping("/save/{userId}")
    public Result saveDuty(@PathVariable Long userId, @RequestBody List<Integer> dutyIdList){
        userDutyRelService.saveUserDuty(userId, dutyIdList);
        return Result.success("职务选择保存成功");
    }
}
