package com.wwl.service;

import com.wwl.model.entity.UserAddress;
import java.util.List;

/**
 * 用户地址业务接口
 */
public interface UserAddressService {

    // 新增地址
    boolean addAddress(UserAddress address);

    // 删除地址
    boolean deleteAddress(Long id);

    // 修改地址
    boolean updateAddress(UserAddress address);

    // 根据id获取地址详情
    UserAddress getAddressById(Long id);

    // 获取用户所有地址列表
    List<UserAddress> getUserAddressList(Long userId);

    // 设置默认地址
    void setDefaultAddress(Long userId, Long addressId);
}
