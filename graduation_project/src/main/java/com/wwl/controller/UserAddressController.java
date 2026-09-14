package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.mapper.UserMapper;
import com.wwl.model.entity.UserAddress;
import com.wwl.service.impl.UserAddressServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户地址控制器
 */
@RestController
@RequestMapping("/address")
public class UserAddressController {

    @Autowired
    private UserAddressServiceImpl userAddressService;

    @Autowired
    private UserMapper userMapper;

    // 新增收货地址
    @PostMapping("/add")
    public Result add(@RequestBody UserAddress address){
        boolean res = userAddressService.addAddress(address);
        return res ? Result.success("地址新增成功") : Result.error("新增失败");
    }

    // 新增收货地址：前端只传 uid，后端内部转换为 userId
    @PostMapping("/addByUid/{uid}")
    public Result addByUid(@PathVariable String uid, @RequestBody UserAddress address) {
        Long userId = userMapper.selectIdByUid(uid);
        if (userId == null) {
            return Result.error("用户不存在");
        }
        address.setUserId(userId);
        boolean res = userAddressService.addAddress(address);
        return res ? Result.success("地址新增成功") : Result.error("新增失败");
    }

    // 删除地址
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id){
        boolean res = userAddressService.deleteAddress(id);
        return res ? Result.success("删除成功") : Result.error("删除失败");
    }

    // 删除地址：使用 uid + addressNo，避免暴露自增地址ID
    @DeleteMapping("/deleteByNo/{uid}/{addressNo}")
    public Result deleteByNo(@PathVariable String uid, @PathVariable String addressNo) {
        Long userId = userMapper.selectIdByUid(uid);
        if (userId == null) {
            return Result.error("用户不存在");
        }
        boolean res = userAddressService.deleteAddressByNo(addressNo, userId);
        return res ? Result.success("删除成功") : Result.error("删除失败");
    }

    // 修改地址
    @PutMapping("/update")
    public Result update(@RequestBody UserAddress address){
        boolean res = userAddressService.updateAddress(address);
        return res ? Result.success("修改成功") : Result.error("修改失败");
    }

    // 修改地址：使用 uid + addressNo 定位，后端校验地址归属
    @PutMapping("/updateByUid/{uid}")
    public Result updateByUid(@PathVariable String uid, @RequestBody UserAddress address) {
        Long userId = userMapper.selectIdByUid(uid);
        if (userId == null) {
            return Result.error("用户不存在");
        }
        UserAddress old = userAddressService.getAddressByNoAndUserId(address.getAddressNo(), userId);
        if (old == null) {
            return Result.error("地址不存在");
        }
        address.setUserId(userId);
        boolean res = userAddressService.updateAddress(address);
        return res ? Result.success("修改成功") : Result.error("修改失败");
    }

    // 根据id查询单条地址
    @GetMapping("/get/{id}")
    public Result getById(@PathVariable("id") Long id){
        return Result.success(userAddressService.getAddressById(id));
    }

    @GetMapping("/getByNo/{uid}/{addressNo}")
    public Result getByNo(@PathVariable("uid") String uid, @PathVariable("addressNo") String addressNo) {
        Long userId = userMapper.selectIdByUid(uid);
        if (userId == null) {
            return Result.error("用户不存在");
        }
        return Result.success(userAddressService.getAddressByNoAndUserId(addressNo, userId));
    }

    // 查询当前用户所有地址列表
    @GetMapping("/list/{userId}")
    public Result list(@PathVariable("userId") Long userId){
        List<UserAddress> list = userAddressService.getUserAddressList(userId);
        return Result.success(list);
    }

    @GetMapping("/listByUid/{uid}")
    public Result listByUid(@PathVariable("uid") String uid) {
        Long userId = userMapper.selectIdByUid(uid);
        if (userId == null) {
            return Result.error("用户不存在");
        }
        List<UserAddress> list = userAddressService.getUserAddressList(userId);
        return Result.success(list);
    }

    // 设置默认地址
    @PutMapping("/default/{userId}/{addressId}")
    public Result setDefault(@PathVariable Long userId,@PathVariable Long addressId){
        userAddressService.setDefaultAddress(userId,addressId);
        return Result.success("默认地址设置成功");
    }

    @PutMapping("/defaultByNo/{uid}/{addressNo}")
    public Result setDefaultByNo(@PathVariable String uid, @PathVariable String addressNo) {
        Long userId = userMapper.selectIdByUid(uid);
        if (userId == null) {
            return Result.error("用户不存在");
        }
        boolean res = userAddressService.setDefaultAddressByNo(userId, addressNo);
        return res ? Result.success("默认地址设置成功") : Result.error("设置失败");
    }
}