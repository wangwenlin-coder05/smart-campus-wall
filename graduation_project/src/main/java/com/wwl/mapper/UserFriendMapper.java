package com.wwl.mapper;

import com.wwl.model.entity.User;
import com.wwl.model.entity.UserFriend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserFriendMapper {
    List<User> searchUser(@Param("keyword") String keyword, @Param("currentUid") String currentUid);

    int countFriend(@Param("userUid") String userUid, @Param("friendUid") String friendUid);

    int countAnyRelation(@Param("userUid") String userUid, @Param("friendUid") String friendUid);

    int insert(UserFriend friend);

    int accept(@Param("id") Long id, @Param("friendUid") String friendUid);

    List<User> listFriends(@Param("userUid") String userUid);

    List<UserFriend> listReceivedRequests(@Param("friendUid") String friendUid);
}
