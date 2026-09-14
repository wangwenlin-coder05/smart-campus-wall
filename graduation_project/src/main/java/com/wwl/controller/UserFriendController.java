package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.mapper.UserFriendMapper;
import com.wwl.model.entity.User;
import com.wwl.model.entity.UserFriend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friend")
public class UserFriendController {

    @Autowired
    private UserFriendMapper userFriendMapper;

    @GetMapping("/search")
    public Result<List<User>> search(@RequestParam String uid, @RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.error("请输入搜索内容");
        }
        return Result.success(userFriendMapper.searchUser(keyword.trim(), uid));
    }

    @PostMapping("/add")
    public Result add(@RequestBody UserFriend friend) {
        if (friend.getUserUid() == null || friend.getFriendUid() == null) {
            return Result.error("参数异常");
        }
        if (friend.getUserUid().equals(friend.getFriendUid())) {
            return Result.error("不能添加自己");
        }
        if (userFriendMapper.countFriend(friend.getUserUid(), friend.getFriendUid()) > 0) {
            return Result.error("已经是好友");
        }
        if (userFriendMapper.countAnyRelation(friend.getUserUid(), friend.getFriendUid()) > 0) {
            return Result.error("好友申请已发送，等待对方同意");
        }
        userFriendMapper.insert(friend);
        return Result.success("好友申请已发送");
    }

    @GetMapping("/list")
    public Result<List<User>> list(@RequestParam String uid) {
        return Result.success(userFriendMapper.listFriends(uid));
    }

    @GetMapping("/requests")
    public Result<List<UserFriend>> requests(@RequestParam String uid) {
        return Result.success(userFriendMapper.listReceivedRequests(uid));
    }

    @PostMapping("/accept")
    public Result accept(@RequestParam Long id, @RequestParam String uid) {
        return userFriendMapper.accept(id, uid) > 0 ? Result.success("已同意好友申请") : Result.error("申请不存在或已处理");
    }
}
