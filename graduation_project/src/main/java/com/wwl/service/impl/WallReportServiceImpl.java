package com.wwl.service.impl;

import com.wwl.model.entity.WallReport;
import com.wwl.mapper.WallReportMapper;
import com.wwl.service.IWallReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子/评论举报 业务实现类
 */
@Service
public class WallReportServiceImpl implements IWallReportService {

    @Autowired
    private WallReportMapper wallReportMapper;

    @Override
    public List<WallReport> getReportList(Long postId, Long commentId, Integer status) {
        return wallReportMapper.selectReportList(postId, commentId, status);
    }

    @Override
    public boolean addReport(WallReport entity) {
        entity.setIsDeleted(0);
        // 默认待处理
        entity.setStatus(0);
        entity.setCreateTime(LocalDateTime.now());
        return wallReportMapper.insertReport(entity) > 0;
    }

    @Override
    public boolean editReport(WallReport entity) {
        return wallReportMapper.updateReport(entity) > 0;
    }

    @Override
    public boolean delReport(Long id) {
        return wallReportMapper.logicDeleteReport(id) > 0;
    }
}