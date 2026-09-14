package com.wwl.service;

import com.wwl.model.entity.User;
import java.util.List;

/**
 * User service API.
 */
public interface UserService {
    boolean addUser(User user);

    User getUserByUid(String uid);

    List<User> getUserList();

    boolean updateUser(User user);

    boolean updateUserByUid(User user);

    boolean deleteUser(String uid);

    List<User> selectUserListByCondition(User user);
}