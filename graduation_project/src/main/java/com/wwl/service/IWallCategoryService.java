package com.wwl.service;

import com.wwl.model.entity.WallCategory;
import java.util.List;

/**
 * 表白墙一级分类 业务接口
 */
public interface IWallCategoryService {

    /**
     * 条件查询分类列表
     * @param name 分类名称（模糊查询，可选）
     * @param isShow 是否展示（0-隐藏 1-展示，可选）
     * @return 分类列表
     */
    List<WallCategory> getCategoryList(String name, Integer isShow);

    /**
     * 根据ID获取分类详情
     * @param id 分类ID
     * @return 分类详情
     */
    WallCategory getCategoryById(Long id);

    /**
     * 新增一级分类
     * @param entity 分类实体
     * @return 操作结果
     */
    boolean addCategory(WallCategory entity);

    /**
     * 修改一级分类
     * @param entity 分类实体
     * @return 操作结果
     */
    boolean editCategory(WallCategory entity);

    /**
     * 逻辑删除
     * @param id 分类ID
     * @return 操作结果
     */
    boolean delCategory(Long id);
}