package com.wwl.service;


import com.wwl.model.entity.Rider;
import java.util.List;

/**
 * 骑手业务接口
 * 定义所有骑手相关业务方法
 */
public interface RiderService {

    // 新增骑手
    boolean addRider(Rider rider);

    // 根据id查询骑手
    Rider getRiderById(Long id);

    // 根据userId查询骑手
    Rider getRiderByUserId(String userId);

    // 查询全部骑手
    List<Rider> getRiderList();

    // 修改骑手信息
    boolean updateRider(Rider rider);

    // 删除骑手
    boolean deleteRider(Long id);

    // 万能多条件查询接口，传什么查什么，不传查全部
    List<Rider> selectRiderListByCondition(Rider rider);
}