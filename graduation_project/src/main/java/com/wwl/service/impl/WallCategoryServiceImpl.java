package com.wwl.service.impl;

import com.wwl.model.entity.WallCategory;
import com.wwl.mapper.WallCategoryMapper;
import com.wwl.service.IWallCategoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 表白墙一级分类 业务实现类
 */
@Service
public class WallCategoryServiceImpl implements IWallCategoryService {

    @Resource
    private WallCategoryMapper wallCategoryMapper;

    @Override
    public List<WallCategory> getCategoryList(String name, Integer isShow) {
        return wallCategoryMapper.selectCategoryList(name, isShow);
    }

    @Override
    public WallCategory getCategoryById(Long id) {
        return wallCategoryMapper.selectCategoryById(id);
    }

    @Override
    public boolean addCategory(WallCategory entity) {
        entity.setIsDeleted(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return wallCategoryMapper.insertCategory(entity) > 0;
    }

    @Override
    public boolean editCategory(WallCategory entity) {
        entity.setUpdateTime(LocalDateTime.now());
        return wallCategoryMapper.updateCategory(entity) > 0;
    }

    @Override
    public boolean delCategory(Long id) {
        return wallCategoryMapper.logicDeleteCategory(id) > 0;
    }
}