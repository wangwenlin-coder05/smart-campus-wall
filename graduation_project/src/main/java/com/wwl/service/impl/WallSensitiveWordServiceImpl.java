package com.wwl.service.impl;

import com.wwl.model.entity.WallSensitiveWord;
import com.wwl.mapper.WallSensitiveWordMapper;
import com.wwl.service.IWallSensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 敏感词库 业务实现类
 */
@Service
public class WallSensitiveWordServiceImpl implements IWallSensitiveWordService {

    @Autowired
    private WallSensitiveWordMapper wallSensitiveWordMapper;

    @Override
    public List<WallSensitiveWord> getSensitiveList(String word) {
        return wallSensitiveWordMapper.selectSensitiveList(word);
    }

    @Override
    public boolean addSensitive(WallSensitiveWord entity) {
        entity.setIsDeleted(0);
        entity.setCreateTime(LocalDateTime.now());
        return wallSensitiveWordMapper.insertSensitive(entity) > 0;
    }

    @Override
    public boolean delSensitive(Long id) {
        return wallSensitiveWordMapper.logicDeleteSensitive(id) > 0;
    }
}