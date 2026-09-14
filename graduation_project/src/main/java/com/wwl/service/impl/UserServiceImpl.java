package com.wwl.service.impl;

import com.wwl.model.entity.User;
import com.wwl.mapper.UserMapper;
import com.wwl.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 用户业务实现�?
 * 处理业务逻辑，调用mapper操作数据�?
 */
@Service
public class UserServiceImpl implements UserService {

    // 自动注入mapper接口，你课堂学的@Autowired
    @Autowired
    private UserMapper userMapper;

    // 新增用户
    @Override
    public boolean addUser(User user) {
        return userMapper.insertUser(user) > 0;
    }

    // 根据UID查询用户
    @Override
    public User getUserByUid(String uid) {
        return userMapper.selectUserByUid(uid);
    }

    // 查询所有用户列表
    @Override
    public List<User> getUserList() {
        return userMapper.selectUserList();
    }

    // 修改用户
    @Override
    public boolean updateUser(User user) {
        return userMapper.updateUser(user) > 0;
    }

    @Override
    public boolean updateUserByUid(User user) {
        return userMapper.updateUserByUid(user) > 0;
    }

    // 删除用户
    @Override
    public boolean deleteUser(String uid) {
        return userMapper.deleteUserByUid(uid) > 0;
    }

    //通用多条件查询用户列表
    @Override
    public List<User> selectUserListByCondition(User user) {
        return userMapper.selectUserListByCondition( user);
    }
}
