package com.wwl.service.impl;

import com.wwl.model.entity.OrderImage;
import com.wwl.model.entity.OrderInfo;
import com.wwl.mapper.OrderImageMapper;
import com.wwl.service.OrderImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderImageServiceImpl implements OrderImageService {

    @Autowired
    private OrderImageMapper orderImageMapper;

    @Override
    public boolean saveOne(OrderImage orderImage) {
        return orderImageMapper.insertImage(orderImage) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSave(Long orderId, List<String> urlList) {
        if(urlList == null || urlList.isEmpty()){
            return true;
        }

        List<OrderImage> list = new ArrayList<>();
        for(String url : urlList){
            OrderImage img = new OrderImage();
            img.setOrderId(orderId);
            img.setImageUrl(url);
            list.add(img);
        }
        return orderImageMapper.batchInsertImage(list) > 0;
    }

    @Override
    public List<OrderImage> getListByOrderId(Long orderId) {
        return orderImageMapper.selectByOrderId(orderId);
    }

    @Override
    public boolean removeById(Long id) {
        return orderImageMapper.deleteById(id) > 0;
    }

    @Override
    public boolean removeByOrderId(Long orderId) {
        return orderImageMapper.deleteByOrderId(orderId) > 0;
    }

    @Override
    public boolean updateImg(Long id, String imageUrl) {
        return orderImageMapper.updateImageUrl(id, imageUrl) > 0;
    }
}
