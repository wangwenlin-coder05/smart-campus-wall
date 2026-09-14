package com.wwl.service.impl;


import com.wwl.mapper.RiderMapper;
import com.wwl.model.entity.Rider;
import com.wwl.service.RiderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 骑手业务实现类
 */
@Service
public class RiderServiceImpl implements RiderService {

    @Autowired
    private RiderMapper riderMapper;

    @Override
    public boolean addRider(Rider rider) {
        return riderMapper.insertRider(rider) > 0;
    }

    @Override
    public Rider getRiderById(Long id) {
        return riderMapper.selectRiderById(id);
    }

    @Override
    public Rider getRiderByUserId(String userId) {
        return riderMapper.selectRiderByUserId(userId);
    }

    @Override
    public List<Rider> getRiderList() {
        return riderMapper.selectRiderList();
    }

    @Override
    public boolean updateRider(Rider rider) {
        return riderMapper.updateRider(rider) > 0;
    }

    @Override
    public boolean deleteRider(Long id) {
        return riderMapper.deleteRiderById(id) > 0;
    }

    @Override
    public List<Rider> selectRiderListByCondition(Rider rider) {
        return riderMapper.selectRiderListByCondition(rider);
    }
}