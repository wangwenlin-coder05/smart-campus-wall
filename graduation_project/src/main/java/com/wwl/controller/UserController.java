package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.mapper.OrganizationActivityMapper;
import com.wwl.mapper.UserFriendMapper;
import com.wwl.mapper.UserMapper;
import com.wwl.model.entity.User;
import com.wwl.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 * 对外提供增删改查接口，Apifox调用测试
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserFriendMapper userFriendMapper;

    @Autowired
    private OrganizationActivityMapper organizationActivityMapper;

    // 新增用户接口  POST请求
    @PostMapping("/add")
    public Result add(@RequestBody User user){
        boolean res = userService.addUser(user);
        if(res){
            return Result.success("新增用户成功");
        }
        return Result.error("新增失败");
    }

    // 根据条件用户  GET请求
    @GetMapping("/get/{id}")
    public Result getById(@PathVariable String id){
        User user = userService.getUserByUid(id);
        return Result.success(user);
    }

    // 万能多条件查询接口，传什么查什么，不传查全部
    @PostMapping("/condition")
    public Result conditionList(@RequestBody User user){
        List<User> list = userService.selectUserListByCondition(user);
        return Result.success(list);
    }

    // 查询全部用户列表  GET请求
    @GetMapping("/list")
    public Result list(){
        List<User> list = userService.getUserList();
        return Result.success(list);
    }

    // 修改用户信息  PUT请求
    @PutMapping("/update")
    public Result update(@RequestBody User user){
        boolean res = userService.updateUser(user);
        if(res){
            return Result.success("修改成功");
        }
        return Result.error("修改失败");
    }

    // 根据UID修改个人信息，前端不需要传数据库id
    @PutMapping("/updateByUid")
    public Result updateByUid(@RequestBody User user){
        if (user.getUid() == null || user.getUid().isBlank()) {
            return Result.error("用户身份异常");
        }
        boolean res = userService.updateUserByUid(user);
        if(res){
            return Result.success("修改成功", userService.getUserByUid(user.getUid()));
        }
        return Result.error("修改失败");
    }

    // 删除用户  DELETE请求
    @DeleteMapping("/delete/{uid}")
    public Result delete(@PathVariable String uid){
        boolean res = userService.deleteUser(uid);
        if(res){
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    // 用户主页：返回用户信息 + 发布的组局 + 是否已关注（好友关系）
    @GetMapping("/home")
    public Result home(@RequestParam String userId, @RequestParam(required = false) String currentUid) {
        User user = userMapper.selectUserByUid(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("name", user.getUsername());
        data.put("avatar", user.getAvatar());
        data.put("uid", user.getUid());
        data.put("groups", organizationActivityMapper.selectCreatedByUser(userId));
        if (currentUid != null && !currentUid.isBlank()) {
            data.put("isFollowing", userFriendMapper.countFriend(currentUid, userId) > 0);
        } else {
            data.put("isFollowing", false);
        }
        return Result.success(data);
    }
}