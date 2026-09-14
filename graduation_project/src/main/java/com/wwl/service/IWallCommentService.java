package com.wwl.service;

import com.wwl.common.result.Result;
import com.wwl.model.entity.WallComment;
import java.util.List;
import java.util.Map;

/**
 * 评论业务接口
 */
public interface IWallCommentService {

    /**
     * 新增评论
     * @param wallComment 评论实体参数
     * @return 统一返回结果
     */
    Result addComment(WallComment wallComment);

    /**
     * 根据帖子ID查询评论列表
     * @param postId 帖子主键ID
     * @return 评论集合（已联表用户信息）
     */
    List<WallComment> listCommentByPostId(Long postId);

    /**
     * 软删除评论
     * @param commentId 评论ID
     * @param currentUserUid 当前登录用户脱敏UID
     * @return 操作结果
     */
    Result deleteComment(Long commentId,String currentUserUid);

    /**
     * 查询帖子的热门评论（点赞数最高的前3条）
     * @param postId 帖子ID
     * @return 热门评论列表（已处理匿名信息）
     */
    List<WallComment> getHotCommentsByPostId(Long postId);

    /**
     * 批量查询：根据帖子ID集合查询所有帖子的热门评论
     * 返回 Map<帖子ID, 该帖子的热门评论列表>，用于解决 N+1 查询问题
     * @param postIdList 帖子ID集合
     * @return 按帖子ID分组的热门评论Map
     */
    Map<Long, List<WallComment>> getHotCommentsByPostIdList(List<Long> postIdList);
}