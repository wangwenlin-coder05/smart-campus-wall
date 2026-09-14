package com.wwl.service.impl;

import com.wwl.model.entity.WallCategorySub;
import com.wwl.mapper.WallCategorySubMapper;
import com.wwl.service.IWallCategorySubService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 表白墙二级子分类 业务实现类
 */
@Service
public class WallCategorySubServiceImpl implements IWallCategorySubService {

    @Resource
    private WallCategorySubMapper wallCategorySubMapper;

    @Override
    public List<WallCategorySub> getSubList(Long categoryId, String name, Integer isShow) {
        return wallCategorySubMapper.selectSubList(categoryId, name, isShow);
    }

    @Override
    public WallCategorySub getSubById(Long id) {
        return wallCategorySubMapper.selectSubById(id);
    }

    @Override
    public boolean addSub(WallCategorySub entity) {
        entity.setIsDeleted(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return wallCategorySubMapper.insertSub(entity) > 0;
    }

    @Override
    public boolean editSub(WallCategorySub entity) {
        entity.setUpdateTime(LocalDateTime.now());
        return wallCategorySubMapper.updateSub(entity) > 0;
    }

    @Override
    public boolean delSub(Long id) {
        return wallCategorySubMapper.logicDeleteSub(id) > 0;
    }
}