package com.wwl.mapper;

import com.wwl.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户Mapper接口�?
 * 只定义方法，SQL全部手写在xml文件�?
 * 原生MyBatis，完全自主可�?
 */
@Mapper
public interface UserMapper {

    // 新增用户信息
    int insertUser(User user);

    // 根据UID查询单个用户
    User selectUserByUid(String uid);

    // 根据UID查询内部数据库ID，前端不直接接触该ID
    Long selectIdByUid(@Param("uid") String uid);

    // 根据内部数据库ID查询UID
    String selectUidById(@Param("id") Long id);

    // 根据手机号查询单个用户，用于登录和注册查重
    User selectUserByPhone(@Param("phone") String phone);

    // 根据登录方式和账号查询用户，支持手机号、邮箱、微信、支付宝
    User selectUserByAccount(@Param("loginType") String loginType, @Param("account") String account);

    // 查询所有用户列表
    List<User> selectUserList();

    // 修改用户信息
    int updateUser(User user);

    // 根据UID修改用户信息
    int updateUserByUid(User user);

    // 根据uid删除用户
    int deleteUserByUid(String uid);

    //通用多条件查询用户列表
    List<User> selectUserListByCondition(User user);
}