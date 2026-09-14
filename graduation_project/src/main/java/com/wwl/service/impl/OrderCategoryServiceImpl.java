package com.wwl.service.impl;

import com.wwl.model.entity.OrderCategory;
import com.wwl.mapper.OrderCategoryMapper;
import com.wwl.service.OrderCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 订单分类业务实现类
 */
@Service
public class OrderCategoryServiceImpl implements OrderCategoryService {

    @Autowired
    private OrderCategoryMapper orderCategoryMapper;

    /**
     * 查询一级分类
     */
    @Override
    public List<OrderCategory> getFirstCategoryList() {
        return orderCategoryMapper.selectFirstCategory();
    }

    /**
     * 查询下级子分类
     */
    @Override
    public List<OrderCategory> getChildCategoryList(Integer parentId) {
        return orderCategoryMapper.selectChildByParentId(parentId);
    }

    /**
     * 新增分类
     */
    @Override
    public boolean addCategory(OrderCategory category) {
        // 返回true代表新增成功
        return orderCategoryMapper.insertCategory(category) > 0;
    }

    /**
     * 修改分类
     */
    @Override
    public boolean editCategory(OrderCategory category) {
        return orderCategoryMapper.updateCategory(category) > 0;
    }

    /**
     * 删除分类
     */
    @Override
    public boolean removeCategory(Integer id) {
        return orderCategoryMapper.deleteCategory(id) > 0;
    }
}
