package com.wwl.service.impl;

import com.wwl.model.entity.OrderDict;
import com.wwl.mapper.OrderDictMapper;
import com.wwl.service.OrderDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 订单分类业务实现
 */
@Service
public class OrderDictServiceImpl implements OrderDictService {

    @Autowired
    private OrderDictMapper orderDictMapper;

    @Override
    public boolean addDict(OrderDict dict) {
        return orderDictMapper.insertDict(dict) > 0;
    }

    @Override
    public OrderDict getById(Integer id) {
        return orderDictMapper.getDictById(id);
    }

    @Override
    public List<OrderDict> getAllList() {
        return orderDictMapper.listAllDict();
    }

    @Override
    public List<OrderDict> conditionList(OrderDict dict) {
        return orderDictMapper.getDictByCondition(dict);
    }

    @Override
    public boolean updateDict(OrderDict dict) {
        return orderDictMapper.updateDict(dict) > 0;
    }

    @Override
    public boolean deleteDict(Integer id) {
        return orderDictMapper.deleteDict(id) > 0;
    }
}
