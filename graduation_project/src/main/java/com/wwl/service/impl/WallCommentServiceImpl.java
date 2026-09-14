package com.wwl.service.impl;

import com.wwl.common.result.Result;
import com.wwl.model.entity.WallAnonymousConfig;
import com.wwl.model.entity.WallComment;
import com.wwl.mapper.WallCommentMapper;
import com.wwl.service.IWallCommentService;
import com.wwl.service.WallAnonymousConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论业务实现类
 * 不使用DTO，直接实体接收
 * 采用软删除，增加本人才能删除评论权限校验
 */
@Service
public class WallCommentServiceImpl implements IWallCommentService {

    @Autowired
    private WallCommentMapper wallCommentMapper;
    @Autowired
    private WallAnonymousConfigService wallAnonymousConfigService;

    /**
     * 新增评论
     */
    @Override
    public Result addComment(WallComment comment) {
        // 通过脱敏UID获取真实用户主键ID
        Long realUserId = wallCommentMapper.getUserIdByUserUid(comment.getUserUid());
        if (realUserId == null) {
            return Result.error("评论用户不存在，发布失败");
        }
        comment.setUserId(realUserId);

        // 处理被回复用户ID
        Long realReplyUserId = 0L;
        if (comment.getReplyUserUid() != null && !"".equals(comment.getReplyUserUid())) {
            realReplyUserId = wallCommentMapper.getUserIdByUserUid(comment.getReplyUserUid());
            if (realReplyUserId == null) {
                realReplyUserId = 0L;
            }
        }
        comment.setReplyUserId(realReplyUserId);

        // 填充默认字段
        if (comment.getIsAnonymous() == null) {
            comment.setIsAnonymous(0);
        }
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }
        if (comment.getReplyCommentId() == null) {
            comment.setReplyCommentId(0L);
        }
        comment.setLikeCount(0);
        comment.setIsDeleted(0);

        int rows = wallCommentMapper.insert(comment);
        return rows > 0 ? Result.success("评论发布成功") : Result.error("评论发布失败");
    }

    /**
     * 查询帖子评论列表
     * 后端统一处理匿名展示：匿名评论覆盖默认头像昵称
     */

    /**
     * 根据帖子ID查询评论列表，并批量处理评论匿名昵称、头像替换
     * @param postId 帖子ID
     * @return 处理完成后的评论列表
     */
    @Override
    public List<WallComment> listCommentByPostId(Long postId) {
        // 1. 根据帖子ID，从数据库查询该帖子下所有评论
        List<WallComment> commentList = wallCommentMapper.listCommentByPostId(postId);
        // 2. 查询正常启用的匿名配置列表（匿名昵称、匿名头像规则）
        List<WallAnonymousConfig> anonymousConfigList = wallAnonymousConfigService.listNormalAnonymousConfig();

        // 3. 遍历所有评论
        for (WallComment comment : commentList) {
            // 调用匿名工具类：自动给评论对象替换匿名昵称、头像
            // 底层就是你刚才看不懂的反射代码，通用赋值，不用挨个set
            AnonymousUtil.handleAnonymous(comment, anonymousConfigList);
        }
        // 返回处理好匿名信息的评论列表
        return commentList;
    }




    /**
     * 软删除评论
     * 校验：只能自己删除自己的评论
     */
    @Override
    public Result deleteComment(Long commentId, String currentUserUid) {
        if (commentId == null || commentId <= 0) {
            return Result.error("评论ID非法");
        }
        if (currentUserUid == null || currentUserUid.isEmpty()) {
            return Result.error("用户身份异常");
        }

        // 查询评论信息
        WallComment comment = wallCommentMapper.getCommentById(commentId);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        if (comment.getIsDeleted() == 1) {
            return Result.error("该评论已删除");
        }

        // 权限校验：只能删除自己的评论
        if (!currentUserUid.equals(comment.getUserUid())) {
            return Result.error("无权删除他人评论");
        }

        // 执行软删除
        int rows = wallCommentMapper.deleteCommentSoft(commentId);
        return rows > 0 ? Result.success("评论删除成功") : Result.error("删除失败");
    }

    /**
     * 查询帖子的热门评论（点赞数最高的前3条）
     * @param postId 帖子ID
     * @return 热门评论列表（已处理匿名信息）
     */
    @Override
    public List<WallComment> getHotCommentsByPostId(Long postId) {
        // 1. 查询热门评论（点赞数最高前3条，点赞相同按时间倒序）
        List<WallComment> hotComments = wallCommentMapper.selectHotCommentsByPostId(postId);
        
        // 2. 查询匿名配置
        List<WallAnonymousConfig> anonymousConfigList = wallAnonymousConfigService.listNormalAnonymousConfig();
        
        // 3. 处理匿名信息
        for (WallComment comment : hotComments) {
            AnonymousUtil.handleAnonymous(comment, anonymousConfigList);
        }
        
        return hotComments;
    }

    /**
     * 批量查询：根据帖子ID集合查询所有帖子的热门评论（Top3）
     * 1次SQL批量查出所有评论 → 内存按帖子ID分组 → 每组取点赞最高的前3条
     * 用于解决帖子列表查询的 N+1 问题
     *
     * @param postIdList 帖子ID集合
     * @return Map<帖子ID, 该帖子的热门评论列表>
     */
    @Override
    public Map<Long, List<WallComment>> getHotCommentsByPostIdList(List<Long> postIdList) {
        // 1. 查询匿名配置（只查一次，全局复用）
        List<WallAnonymousConfig> anonymousConfigList = wallAnonymousConfigService.listNormalAnonymousConfig();

        // 2. 批量查询所有帖子的评论（1次SQL，替代N次逐个查询）
        List<WallComment> allComments = wallCommentMapper.selectHotCommentsByPostIdList(postIdList);

        // 3. 内存分组：按帖子ID分组
        Map<Long, List<WallComment>> commentGroupMap = allComments.stream()
                .collect(Collectors.groupingBy(WallComment::getPostId));

        // 4. 遍历每个分组，处理匿名信息 + 截取Top3
        Map<Long, List<WallComment>> resultMap = new HashMap<>();
        for (Map.Entry<Long, List<WallComment>> entry : commentGroupMap.entrySet()) {
            List<WallComment> comments = entry.getValue();

            // 处理匿名信息
            for (WallComment comment : comments) {
                AnonymousUtil.handleAnonymous(comment, anonymousConfigList);
            }

            // 按点赞数降序排序（SQL已排序，这里做兜底保证），取前3条
            comments.sort((a, b) -> {
                int likeCompare = b.getLikeCount().compareTo(a.getLikeCount());
                if (likeCompare != 0) {
                    return likeCompare;
                }
                return b.getCreateTime().compareTo(a.getCreateTime());
            });

            // 取Top3热门评论
            int topN = Math.min(comments.size(), 3);
            resultMap.put(entry.getKey(), comments.subList(0, topN));
        }

        return resultMap;
    }
}