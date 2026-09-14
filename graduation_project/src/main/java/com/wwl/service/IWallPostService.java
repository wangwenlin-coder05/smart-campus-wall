package com.wwl.service;

import com.wwl.model.entity.WallPost;

import java.util.List;
import java.util.Map;

/**
 * 表白墙帖子 业务接口
 */
public interface IWallPostService {
    /**
     * 查询帖子列表（支持多条件筛选）
     * @param userId 用户ID（可选）
     * @param categoryIdList 一级分类ID集合（单独选一级大类使用）
     * @param groupList 一级+二级分类组合Map集合（精准筛选二级使用）
     * @param status 审核状态（可选）
     * @param isAnonymous 是否匿名（可选）
     * @param keyword 模糊搜索关键词
     * @return 帖子列表
     */
    List<WallPost> getPostList(Long userId,
                               List<Long> categoryIdList,
                               List<Map<String,Long>> groupList,
                               Integer status,
                               Integer isAnonymous,
                               String keyword,
                               String currentUserUid);

    
    /**
     * 根据ID获取帖子详情
     * @param id 帖子ID
     * @return 帖子详情
     */
    WallPost getPostById(Long id);

    /**
     * 根据用户ID查询用户发布过的帖子列表
     * @param UID 用户UID
     * @return 帖子列表
     */
    List<WallPost> getPostListByUserId(String UID, Integer pageNum, Integer pageSize);

    /**
     * 鏍规嵁鐢ㄦ埛UID缁熻鍙戝竷甯栧瓙鏁伴噺銆佹祻瑙堛€佺偣璧炪€佽瘎璁恒€?
     * @param UID 鐢ㄦ埛UID
     * @return 缁熻缁撴灉
     */
    Map<String, Object> getPostStatsByUserId(String UID);


    
    /**
     * 新增帖子 附带批量保存媒体文件
     * @param entity 帖子实体
     * @return 操作结果
     */
    boolean addPost(WallPost entity);
    
    /**
     * 编辑帖子 先删旧附件再存新数组
     * @param entity 帖子实体
     * @return 操作结果
     */
    boolean editPost(WallPost entity);
    
    /**
     * 逻辑删除帖子 + 删除对应附件
     * @param id 帖子ID
     * @return 操作结果
     */
    boolean delPost(Long id);
    
    /**
     * 校验用户当日发帖数是否超过限制（最多100条）
     * @param userUid 用户UID
     * @return true-未超限 false-已超限
     */
    boolean checkUserPostLimit(String userUid);


}
