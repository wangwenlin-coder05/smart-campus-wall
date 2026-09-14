package com.wwl.service.impl;

import com.github.pagehelper.PageHelper;
import com.wwl.common.util.AnonymousUtil;
import com.wwl.mapper.WallPostMapper;
import com.wwl.mapper.WallPostMediaMapper;
import com.wwl.model.entity.*;
import com.wwl.service.IWallCommentService;
import com.wwl.service.IWallPostService;
import com.wwl.service.UserService;
import com.wwl.service.WallAnonymousConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 表白墙帖子 业务实现类
 * 支持媒体附件管理（图片/视频/音频）
 */
@Slf4j
@Service
public class WallPostServiceImpl implements IWallPostService {

    @Autowired
    private WallPostMapper wallPostMapper;

    @Autowired
    private WallPostMediaMapper wallPostMediaMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private WallAnonymousConfigService wallAnonymousConfigService;

    @Autowired
    private IWallCommentService wallCommentService;
    // 全局随机对象，避免频繁创建消耗性能
    private final Random random = new Random();

    /**
     * 获取帖子列表并处理媒体封装、匿名信息替换
     * 批量查询优化：先收集帖子ID → 批量IN查询媒体和评论 → 内存分组组装
     * 将 N 条 SQL（2N+1）降为 3 条，大幅减少数据库网络IO
     *
     * @param userId        用户ID
     * @param categoryIdList 一级分类ID集合（单独选一级大类使用）
     * @param groupList      一二分类组合集合（精准筛选二级使用）
     * @param status        帖子状态
     * @param isAnonymous   是否匿名
     * @param keyword       搜索关键词
     * @return 处理完成的帖子列表
     */
    @Override
    public List<WallPost> getPostList(Long userId, List<Long> categoryIdList, List<Map<String, Long>> groupList, Integer status, Integer isAnonymous, String keyword, String currentUserUid) {
        log.info("getPostList查询参数: userId={}, categoryIdList={}, groupList={}, status={}, isAnonymous={}, keyword={}", userId, categoryIdList, groupList, status, isAnonymous, keyword);

        // 核心互斥：有组合筛选就清空一级列表，防止条件叠加出错
        if (groupList != null && !groupList.isEmpty()) {
            categoryIdList = null;
        }

        // ========== 第1步：查询帖子主列表（1次SQL） ==========
        List<WallPost> postList = wallPostMapper.selectPostList(userId, categoryIdList, groupList, status, isAnonymous, keyword, currentUserUid);

        // ========== 第2步：批量装配媒体、评论、匿名信息 ==========
        enrichPostList(postList);

        return postList;
    }

    /**
     * 批量装配帖子附加信息：媒体URL、匿名处理、热门评论
     * 提取为公共方法，供 getPostList 和 getPostListByUserId 共用
     * 批量 IN 查询，将 N 条 SQL 降为 3 条
     */
    private void enrichPostList(List<WallPost> postList) {
        if (postList == null || postList.isEmpty()) {
            return;
        }

        // 提取所有帖子ID集合，用于后续批量IN查询
        List<Long> postIdList = new ArrayList<>();
        for (WallPost post : postList) {
            postIdList.add(post.getId());
        }

        // 批量查询所有帖子的媒体URL（1次SQL，替代N次单查）
        List<WallPostMedia> allMediaList = wallPostMediaMapper.selectMediaUrlByPostIdList(postIdList);

        // 内存分组：按帖子ID分组，构建 Map<postId, List<mediaUrl>>
        Map<Long, List<String>> mediaGroupMap = new HashMap<>();
        for (WallPostMedia media : allMediaList) {
            mediaGroupMap
                    .computeIfAbsent(media.getPostId(), k -> new ArrayList<>())
                    .add(media.getMediaUrl());
        }

        // 批量查询所有帖子的热门评论（1次SQL，替代N次单查）
        // 返回 Map<postId, List<WallComment>>，每组已取 Top3 并处理匿名
        Map<Long, List<WallComment>> commentGroupMap = wallCommentService.getHotCommentsByPostIdList(postIdList);

        // 查询匿名配置（只查1次，全局复用）
        List<WallAnonymousConfig> anonymousConfigList = wallAnonymousConfigService.listNormalAnonymousConfig();

        // 内存组装：将批量查询结果设置到对应帖子中
        for (WallPost post : postList) {
            Long postId = post.getId();

            // 从分组Map中获取当前帖子的媒体列表
            List<String> mediaUrlList = mediaGroupMap.getOrDefault(postId, new ArrayList<>());
            post.setMediaUrlList(mediaUrlList);

            // 调用通用工具类处理匿名
            String[] anonymousInfo = AnonymousUtil.handleAnonymous(
                    post.getIsAnonymous(),
                    post.getNickname(),
                    post.getAvatar(),
                    anonymousConfigList
            );
            post.setNickname(anonymousInfo[0]);
            post.setAvatar(anonymousInfo[1]);

            // 从分组Map中获取当前帖子的热门评论
            List<WallComment> hotComments = commentGroupMap.getOrDefault(postId, new ArrayList<>());
            post.setHotComments(hotComments);
        }
    }

    @Override
    public WallPost getPostById(Long id) {
        WallPost post = wallPostMapper.selectPostById(id);
        if (post != null) {
            // 获取媒体列表
            List<String> urlList = wallPostMediaMapper.selectMediaUrlByPostId(id);
            post.setMediaUrlList(urlList);
            
            // 获取热门评论
            List<WallComment> hotComments = wallCommentService.getHotCommentsByPostId(id);
            post.setHotComments(hotComments);
        }
        return post;
    }

    /**
     * 根据用户UID查询用户发布过的帖子列表
     * @param UID 用户UID
     * @return 帖子列表
     */
    @Override
    public List<WallPost> getPostListByUserId(String UID, Integer pageNum, Integer pageSize) {
        // 根据UID查询用户
        User user = userService.getUserByUid(UID);

        if (user == null) {
            log.info("查询失败：用户不存在，UID={}", UID);
            return null;
        }

        // 开启分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<WallPost> postList = wallPostMapper.selectPostListByUserId(user.getId());

        // 复用批量装配方法：媒体、匿名、热门评论
        enrichPostList(postList);

        return postList;
    }

    @Override
    public Map<String, Object> getPostStatsByUserId(String UID) {
        Map<String, Object> emptyStats = new HashMap<>();
        emptyStats.put("total", 0);
        emptyStats.put("views", 0);
        emptyStats.put("likes", 0);
        emptyStats.put("comments", 0);

        User user = userService.getUserByUid(UID);
        if (user == null) {
            log.info("查询统计失败：用户不存在，UID={}", UID);
            return emptyStats;
        }

        Map<String, Object> stats = wallPostMapper.selectUserPostStats(user.getId());
        return stats == null ? emptyStats : stats;
    }

    /**
     * 发帖：先存主表拿ID → 批量存附件
     * 事务保证一致性
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPost(WallPost entity) {
        // 0. 必填参数校验
        if (entity.getUserUid() == null || entity.getUserUid().isEmpty()) {
            System.out.println("发帖失败：userUid为空");
            return false;
        }
        if (entity.getCategoryId() == null) {
            System.out.println("发帖失败：categoryId为空");
            return false;
        }
        if (entity.getSubCategoryId() == null) {
            System.out.println("发帖失败：subCategoryId为空");
            return false;
        }
        if (entity.getIsAnonymous() == null) {
            System.out.println("发帖失败：isAnonymous为空");
            return false;
        }
        if (entity.getContent() == null || entity.getContent().trim().isEmpty()) {
            System.out.println("发帖失败：content为空");
            return false;
        }
        
        // 0.1 根据 userUid 查询用户，获取 userId 和用户信息
        User user = userService.getUserByUid(entity.getUserUid());
        if (user == null) {
            System.out.println("发帖失败：用户不存在，userUid=" + entity.getUserUid());
            return false;
        }
        entity.setUserId(user.getId());
        if (entity.getPostNo() == null || entity.getPostNo().isEmpty()) {
            entity.setPostNo(generatePostNo());
        }
        
        // 0.2 自动填充昵称和头像（从用户表获取）
//        if (entity.getNickname() == null || entity.getNickname().isEmpty()) {
//            entity.setNickname(user.getNickname());
//        }
//        if (entity.getAvatar() == null || entity.getAvatar().isEmpty()) {
//            entity.setAvatar(user.getAvatar());
//        }
//
        // 0.3 初始化帖子默认值（无论实名/匿名，数据库字段都应该是完整的）
        LocalDateTime now = LocalDateTime.now();
        entity.setViewCount(0);                    // 浏览量初始为0
        entity.setLikeCount(0);                     // 点赞数初始为0
        entity.setCollectCount(0);                  // 收藏数初始为0
        entity.setCommentCount(0);                  // 评论数初始为0
        entity.setStatus(entity.getStatus() != null ? entity.getStatus() : 1);  // 审核状态，默认待审核，测试阶段，直接审核通过，后期要修改
        entity.setIsTop(entity.getIsTop() != null ? entity.getIsTop() : 0);       // 是否置顶，默认否
        entity.setIsTiming(entity.getIsTiming() != null ? entity.getIsTiming() : 0); // 发布类型，默认立即发布
        entity.setIsDeleted(0);                     // 删除标记，默认未删除
        entity.setCreateTime(now);                  // 创建时间
        entity.setUpdateTime(now);                  // 更新时间
        
        // 1. 插入帖子主表，获取自增ID
        int insert = wallPostMapper.insertPost(entity);
        if (insert <= 0) {
            System.out.println("发帖失败：数据库插入失败");
            return false;
        }
        
        // 2. 批量插入媒体附件
        List<String> mediaList = entity.getMediaUrlList();
        if (mediaList != null && !mediaList.isEmpty()) {
            List<WallPostMedia> mediaEntityList = new ArrayList<>();
            for (int i = 0; i < mediaList.size(); i++) {
                WallPostMedia media = new WallPostMedia();
                media.setPostId(entity.getId());
                media.setMediaUrl(mediaList.get(i));
                media.setSort(i);
                mediaEntityList.add(media);
            }
            wallPostMediaMapper.batchInsert(mediaEntityList);
        }
        
        System.out.println("发帖成功：postNo=" + entity.getPostNo() + ", userUid=" + entity.getUserUid());
        return true;
    }

    /**
     * 生成前端可见的帖子编号，避免暴露数据库自增ID。
     */
    private String generatePostNo() {
        return "P" + System.currentTimeMillis() + String.format("%04d", random.nextInt(10000));
    }


    /**
     * 编辑帖子：先删旧附件 → 更新主表 → 新增新附件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean editPost(WallPost entity) {
        // 1. 删除原有所有媒体附件
        wallPostMediaMapper.deleteMediaByPostId(entity.getId());
        // 2. 更新帖子主表信息
        entity.setUpdateTime(LocalDateTime.now());
        int update = wallPostMapper.updatePost(entity);
        if (update <= 0) {
            return false;
        }
        // 3. 重新批量插入新媒体数组
        List<String> mediaList = entity.getMediaUrlList();
        if (mediaList != null && !mediaList.isEmpty()) {
            List<WallPostMedia> mediaEntityList = new ArrayList<>();
            for (int i = 0; i < mediaList.size(); i++) {
                WallPostMedia media = new WallPostMedia();
                media.setPostId(entity.getId());
                media.setMediaUrl(mediaList.get(i));
                media.setSort(i);
                mediaEntityList.add(media);
            }
            wallPostMediaMapper.batchInsert(mediaEntityList);
        }
        return true;
    }

    /**
     * 删除帖子：逻辑删主表 + 物理清空附件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delPost(Long id) {
        // 逻辑删除帖子
        int del = wallPostMapper.logicDeletePost(id);
        if (del <= 0) {
            return false;
        }
        // 直接物理删除所有关联附件
        wallPostMediaMapper.deleteMediaByPostId(id);
        return true;
    }

    @Override
    public boolean checkUserPostLimit(String userUid) {
        // 根据 userUid 查询用户，获取 userId
        if (userUid == null || userUid.isEmpty()) {
            return false;
        }
        User user = userService.getUserByUid(userUid);
        if (user == null) {
            return false;
        }

        // 计算今天的开始和结束时间
        LocalDateTime dayStart = LocalDate.now().atStartOfDay(); // 今天 00:00:00
        LocalDateTime dayEnd = LocalDate.now().atTime(23, 59, 59); // 今天 23:59:59

        int count = wallPostMapper.countUserDayPost(user.getId(), dayStart, dayEnd);
        System.out.println("=== 发帖限制检查 ===");
        System.out.println("用户UID: " + userUid);
        System.out.println("用户ID: " + user.getId());
        System.out.println("今天开始时间: " + dayStart);
        System.out.println("今天结束时间: " + dayEnd);
        System.out.println("今日已发帖数: " + count);
        System.out.println("是否超限: " + (count >= 100));
        System.out.println("==================");
        return count < 100;//测试阶段，先写100条，测完改回10条
    }
}
