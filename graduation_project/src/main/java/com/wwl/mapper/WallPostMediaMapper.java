package com.wwl.mapper;

import com.wwl.model.entity.WallPostMedia;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WallPostMediaMapper {
    /**
     * 批量插入附件
     */
    int batchInsert(@Param("list") List<WallPostMedia> list);

    /**
     * 根据帖子ID查询所有资源URL
     */
    List<String> selectMediaUrlByPostId(@Param("postId") Long postId);

    /**
     * 批量查询：根据帖子ID集合查询所有媒体记录
     * 返回 List<WallPostMedia>，由 Service 层按 postId 分组
     * 用于解决帖子列表查询的 N+1 问题
     */
    List<WallPostMedia> selectMediaUrlByPostIdList(@Param("postIdList") List<Long> postIdList);

    /**
     * 根据帖子ID物理删除所有附?
     */
    int deleteMediaByPostId(@Param("postId") Long postId);
}