package com.wwl.service;

import com.wwl.model.entity.WallCategorySub;
import java.util.List;

/**
 * 表白墙二级子分类 业务接口
 */
public interface IWallCategorySubService {

    /**
     * 条件查询二级分类列表
     * @param categoryId 一级分类ID（可选）
     * @param name 子分类名称（模糊查询，可选）
     * @param isShow 是否展示（0-隐藏 1-展示，可选）
     * @return 子分类列表
     */
    List<WallCategorySub> getSubList(Long categoryId, String name, Integer isShow);

    /**
     * 根据ID获取子分类详情
     * @param id 子分类ID
     * @return 子分类详情
     */
    WallCategorySub getSubById(Long id);

    /**
     * 新增二级分类
     * @param entity 子分类实体
     * @return 操作结果
     */
    boolean addSub(WallCategorySub entity);

    /**
     * 修改二级分类
     * @param entity 子分类实体
     * @return 操作结果
     */
    boolean editSub(WallCategorySub entity);

    /**
     * 逻辑删除二级分类
     * @param id 子分类ID
     * @return 操作结果
     */
    boolean delSub(Long id);
}