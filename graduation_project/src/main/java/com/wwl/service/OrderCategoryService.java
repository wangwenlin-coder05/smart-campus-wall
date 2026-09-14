package com.wwl.service;

import com.wwl.model.entity.OrderCategory;
import java.util.List;

/**
 * 订单分类业务接口
 */
public interface OrderCategoryService {

    /**
     * 获取全部一级分类列表
     */
    List<OrderCategory> getFirstCategoryList();

    /**
     * 根据父id获取对应二级子分类
     */
    List<OrderCategory> getChildCategoryList(Integer parentId);

    /**
     * 新增分类
     */
    boolean addCategory(OrderCategory category);

    /**
     * 修改分类信息
     */
    boolean editCategory(OrderCategory category);

    /**
     * 删除分类
     */
    boolean removeCategory(Integer id);
}
