package com.wwl.service;

import com.wwl.model.entity.OrderDict;
import java.util.List;

/**
 * 订单分类业务接口
 */
public interface OrderDictService {
    boolean addDict(OrderDict dict);
    OrderDict getById(Integer id);
    List<OrderDict> getAllList();
    List<OrderDict> conditionList(OrderDict dict);
    boolean updateDict(OrderDict dict);
    boolean deleteDict(Integer id);
}
