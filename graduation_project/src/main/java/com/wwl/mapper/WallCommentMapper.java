package com.wwl.mapper;

import com.wwl.model.entity.WallComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WallCommentMapper {

    /**
     * 插入评论数据
     * @param wallComment 评论实体
     * @return 受影响行�?
     */
    int insert(WallComment wallComment);

    /**
     * 根据帖子ID查询评论列表
     * 联表用户表，带出评论人、被回复人昵称头�?
     * @param postId 帖子ID
     * @return 评论集合
     */
    List<WallComment> listCommentByPostId(@Param("postId") Long postId);

    /**
     * 根据脱敏userUid查询真实用户主键ID
     * @param userUid 对外脱敏UID
     * @return 数据库真实userId
     */
    Long getUserIdByUserUid(@Param("userUid") String userUid);

    /**
     * 根据评论ID查询评论信息（用于删除权限校验）
     * @param id 评论ID
     * @return 评论实体
     */
    WallComment getCommentById(@Param("id") Long id);

    /**
     * 评论软删�?
     * @param commentId 评论ID
     * @return 受影响行�?
     */
    int deleteCommentSoft(@Param("commentId") Long commentId);

    /**
     * 查询帖子的热门评论（点赞数最高的前3条，点赞相同按时间倒序）
     * @param postId 帖子ID
     * @return 热门评论列表
     */
    List<WallComment> selectHotCommentsByPostId(@Param("postId") Long postId);

    /**
     * 批量查询：根据帖子ID集合查询所有评论
     * 1次SQL查回所有评论，由 Service 层按帖子分组并取 Top3
     * 用于解决帖子列表查询的 N+1 问题
     * @param postIdList 帖子ID集合
     * @return 所有帖子的评论列表
     */
    List<WallComment> selectHotCommentsByPostIdList(@Param("postIdList") List<Long> postIdList);

    /**
     * 插入评论点赞动作
     * @param commentId 评论ID
     * @param userUid 用户UID
     * @return 受影响行数
     */
    int insertLikeAction(@Param("commentId") Long commentId, @Param("userUid") String userUid);

    /**
     * 删除评论点赞动作
     * @param commentId 评论ID
     * @param userUid 用户UID
     * @return 受影响行数
     */
    int deleteLikeAction(@Param("commentId") Long commentId, @Param("userUid") String userUid);

    /**
     * 修改评论点赞数
     * @param commentId 评论ID
     * @param delta 变化量（1或-1）
     * @return 受影响行数
     */
    int changeLikeCount(@Param("commentId") Long commentId, @Param("delta") int delta);
}