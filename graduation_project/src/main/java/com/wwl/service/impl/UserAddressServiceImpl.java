package com.wwl.service.impl;

import com.wwl.mapper.UserAddressMapper;
import com.wwl.model.entity.UserAddress;
import com.wwl.service.UserAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public boolean addAddress(UserAddress address) {
        if (address.getAddressNo() == null || address.getAddressNo().isBlank()) {
            address.setAddressNo(generateAddressNo());
        }
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            userAddressMapper.cancelAllDefault(address.getUserId());
        }
        return userAddressMapper.insert(address) > 0;
    }

    @Override
    public boolean deleteAddress(Long id) {
        return userAddressMapper.deleteById(id) > 0;
    }

    public boolean deleteAddressByNo(String addressNo, Long userId) {
        return userAddressMapper.deleteByAddressNo(addressNo, userId) > 0;
    }

    @Override
    public boolean updateAddress(UserAddress address) {
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            userAddressMapper.cancelAllDefault(address.getUserId());
        }
        return userAddressMapper.update(address) > 0;
    }

    @Override
    public UserAddress getAddressById(Long id) {
        return userAddressMapper.getById(id);
    }

    public UserAddress getAddressByNo(String addressNo) {
        return userAddressMapper.getByAddressNo(addressNo);
    }

    public UserAddress getAddressByNoAndUserId(String addressNo, Long userId) {
        return userAddressMapper.getByAddressNoAndUserId(addressNo, userId);
    }

    @Override
    public List<UserAddress> getUserAddressList(Long userId) {
        return userAddressMapper.listByUserId(userId);
    }

    @Transactional
    @Override
    public void setDefaultAddress(Long userId, Long addressId) {
        userAddressMapper.cancelAllDefault(userId);
        UserAddress addr = userAddressMapper.getById(addressId);
        if (addr != null) {
            userAddressMapper.setDefault(userId, addr.getAddressNo());
        }
    }

    @Transactional
    public boolean setDefaultAddressByNo(Long userId, String addressNo) {
        userAddressMapper.cancelAllDefault(userId);
        return userAddressMapper.setDefault(userId, addressNo) > 0;
    }

    private String generateAddressNo() {
        return "A" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
