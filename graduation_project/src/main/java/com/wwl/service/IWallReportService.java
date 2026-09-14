package com.wwl.service;

import com.wwl.model.entity.WallReport;
import java.util.List;

/**
 * 帖子/评论举报 业务接口
 */
public interface IWallReportService {

    /**
     * 动态条件查询举报列表
     * @param postId 帖子ID（可选）
     * @param commentId 评论ID（可选）
     * @param status 处理状态（可选）
     * @return 举报记录列表
     */
    List<WallReport> getReportList(Long postId, Long commentId, Integer status);

    /**
     * 新增举报记录
     * @param entity 举报实体
     * @return 操作结果
     */
    boolean addReport(WallReport entity);

    /**
     * 修改举报处理状态
     * @param entity 举报实体
     * @return 操作结果
     */
    boolean editReport(WallReport entity);

    /**
     * 逻辑删除举报
     * @param id 举报ID
     * @return 操作结果
     */
    boolean delReport(Long id);
}