package com.wwl.service.impl;

import com.wwl.mapper.RiderOrderMapper;
import com.wwl.model.entity.RiderOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RiderOrderService {

    @Autowired
    private RiderOrderMapper riderOrderMapper;

    /**
     * 新增骑手订单
     */
    public int addRiderOrder(RiderOrder riderOrder){
        // 默认初始
        riderOrder.setIsDeleted(0);
        riderOrder.setCreateTime(java.time.LocalDateTime.now());
        riderOrder.setUpdateTime(java.time.LocalDateTime.now());
        return riderOrderMapper.insertRiderOrder(riderOrder);
    }

    /**
     * 根据id查询
     */
    public RiderOrder getById(Long id){
        return riderOrderMapper.selectById(id);
    }



    /**
     * 根据主订单id查询
     */
    public RiderOrder getByOrderId(Long orderId){
        return riderOrderMapper.selectByOrderId(orderId);
    }

    /**
     * 更新订单
     */
    public int updateOrder(RiderOrder riderOrder){
        riderOrder.setUpdateTime(java.time.LocalDateTime.now());
        return riderOrderMapper.updateRiderOrder(riderOrder);
    }

    /**
     * 逻辑删除
     */
    public int logicDelete(Long id){
        return riderOrderMapper.deleteLogicById(id);
    }
}
