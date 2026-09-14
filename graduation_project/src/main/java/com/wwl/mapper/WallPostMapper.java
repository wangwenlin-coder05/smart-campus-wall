package com.wwl.mapper;

import com.wwl.model.entity.WallPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 琛ㄧ櫧澧欏笘锟?Mapper
 */
@Mapper
public interface WallPostMapper {

    /**
     * 鍔ㄦ€佹潯浠舵煡璇㈠笘瀛愬垪
     * @param userId 鐢ㄦ埛ID
     * @param categoryIdList 涓€绾у垎绫籌D闆嗗悎锛堝崟鐙€変竴绾уぇ绫讳娇鐢級
     * @param groupList 涓€浜屽垎绫荤粍鍚堥泦鍚堬紙绮惧噯绛涢€変簩绾т娇鐢級
     * @param status 瀹℃牳鐘?
     * @param isAnonymous 鏄惁鍖垮悕
     * @param keyword 妯＄硦鎼滅储鍏抽敭
     */
    List<WallPost> selectPostList(@Param("userId") Long userId,
                                  @Param("categoryIdList") List<Long> categoryIdList,
                                  @Param("groupList") List<Map<String,Long>> groupList,
                                  @Param("status") Integer status,
                                  @Param("isAnonymous") Integer isAnonymous,
                                  @Param("keyword") String keyword,
                                  @Param("currentUserUid") String currentUserUid);


    /**
     * 鏍规嵁ID鏌ヨ甯栧瓙璇︽儏
     */
    WallPost selectPostById(@Param("id") Long id);

    /**
     * 根据公开帖子编号查询内部帖子ID。
     */
    Long selectIdByPostNo(@Param("postNo") String postNo);

    /**
     * 浏览帖子时增加浏览量。
     */
    int increaseViewCount(@Param("postNo") String postNo);

    /**
     * 点赞/取消点赞，delta 只能传 1 或 -1。
     */
    int changeLikeCount(@Param("postNo") String postNo, @Param("delta") Integer delta);

    /**
     * 收藏/取消收藏，delta 只能传 1 或 -1。
     */
    int changeCollectCount(@Param("postNo") String postNo, @Param("delta") Integer delta);

    /**
     * 评论新增/删除后同步帖子评论数。
     */
    int changeCommentCount(@Param("id") Long id, @Param("delta") Integer delta);

    int insertAction(@Param("postNo") String postNo,
                     @Param("userUid") String userUid,
                     @Param("actionType") String actionType);

    int deleteAction(@Param("postNo") String postNo,
                     @Param("userUid") String userUid,
                     @Param("actionType") String actionType);

    /**
     * AI 助手用：多关键词 OR 搜索帖子（内容 + 昵称 + 分类名）
     */
    List<WallPost> selectByKeywords(@Param("keywords") List<String> keywords, @Param("limit") int limit);

    /**
     * 鏂板甯栧瓙
     */

    int insertPost(WallPost entity);

    /**
     * 淇敼甯栧瓙
     */
    int updatePost(WallPost entity);

    /**
     * 閫昏緫鍒犻櫎甯栧瓙
     */
    int logicDeletePost(@Param("id") Long id);

    /**
     * 缁熻鐢ㄦ埛褰撴棩鎶曠鏁伴噺 闄愬埗姣忔棩10锟?
     * @param userId 鐢ㄦ埛ID
     * @param dayStart 褰撳ぉ寮€濮嬫椂锟?yyyy-MM-dd 00:00:00
     * @param dayEnd 褰撳ぉ缁撴潫鏃堕棿 yyyy-MM-dd 23:59:59
     */
    int countUserDayPost(@Param("userId") Long userId, 
                         @Param("dayStart") java.time.LocalDateTime dayStart,
                         @Param("dayEnd") java.time.LocalDateTime dayEnd);

    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ鐢ㄦ埛鍙戝竷杩囩殑甯栧瓙鍒楄〃
     * @param userId 鐢ㄦ埛ID
     * @return 甯栧瓙鍒楄〃
     */
    List<WallPost> selectPostListByUserId(Long userId);

    /**
     * Count all non-deleted posts and interactions for a user's published wall posts.
     */
    Map<String, Object> selectUserPostStats(@Param("userId") Long userId);
}