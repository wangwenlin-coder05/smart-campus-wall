package com.wwl.mapper;

import com.wwl.model.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * User address mapper.
 */
@Mapper
public interface UserAddressMapper {
    int insert(UserAddress address);

    int deleteById(Long id);

    int deleteByAddressNo(@Param("addressNo") String addressNo, @Param("userId") Long userId);

    int update(UserAddress address);

    UserAddress getById(Long id);

    UserAddress getByAddressNo(@Param("addressNo") String addressNo);

    UserAddress getByAddressNoAndUserId(@Param("addressNo") String addressNo, @Param("userId") Long userId);

    List<UserAddress> listByUserId(Long userId);

    int cancelAllDefault(Long userId);

    int setDefault(@Param("userId") Long userId, @Param("addressNo") String addressNo);
}