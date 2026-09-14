package com.wwl.service;

import com.wwl.model.entity.OrderImage;
import java.util.List;

public interface OrderImageService {

    // 单张保存
    boolean saveOne(OrderImage orderImage);

    // 批量保存多张图片
    boolean batchSave(Long orderId, List<String> urlList);

    // 查询订单所有图片
    List<OrderImage> getListByOrderId(Long orderId);

    // 删除单张
    boolean removeById(Long id);

    // 删除订单全部图片
    boolean removeByOrderId(Long orderId);

    // 修改图片地址
    boolean updateImg(Long id, String imageUrl);
}
