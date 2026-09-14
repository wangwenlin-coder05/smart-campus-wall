package com.wwl.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wwl.common.result.Result;
import com.wwl.mapper.WallPostMapper;
import com.wwl.model.entity.ChatMessage;
import com.wwl.model.entity.WallPost;
import com.wwl.service.ChatMessageService;
import com.wwl.service.IWallPostService;
import com.wwl.service.UserService;
import com.wwl.websocket.handler.ChatWebSocketHandler;

/**
 * 表白墙帖子控制器
 * 提供帖子的发布、查询、修改、删除接口
 */
@RestController
@RequestMapping("/wall/post")
public class WallPostController {

    @Autowired
    private IWallPostService wallPostService;

    @Autowired
    private WallPostMapper wallPostMapper;

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private UserService userService;

    /**
     * 查询帖子列表（支持多条件筛选 + 分页 + 关键词模糊搜索）
     * @param pageNum 页码（默认1）
     * @param pageSize 每页大小（默认10）
     * @param userId 用户ID（可选）
     * @param categoryIdList 一级分类ID集合（单独点击一级大类使用）
     * @param categoryGroup 一二分类组合字符串，格式：1-3,2-3（精准筛选二级使用）
     * @param status 审核状态（0-待审核 1-已通过 2-已拒绝，可选）
     * @param isAnonymous 是否匿名（0-否 1-是，可选）
     * @param keyword 模糊搜索关键词：帖子内容/发布昵称/一二分类名称
     * @return 分页帖子列表
     */
    @GetMapping("/list")
    public Result list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) List<Long> categoryIdList,
            @RequestParam(required = false) String categoryGroup,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isAnonymous,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String currentUserUid
    ){
        PageHelper.startPage(pageNum, pageSize);

        // 解析组合参数，转为 Map 集合
        List<Map<String, Long>> groupList = new ArrayList<>();
        if (StringUtils.hasText(categoryGroup)) {
            String[] splitGroup = categoryGroup.split(",");
            for (String group : splitGroup) {
                String[] arr = group.split("-");
                if (arr.length == 2) {
                    Map<String, Long> map = new HashMap<>();
                    map.put("categoryId", Long.valueOf(arr[0]));
                    map.put("subCategoryId", Long.valueOf(arr[1]));
                    groupList.add(map);
                }
            }
        }

        // 调用修改后的 Service，传入 groupList，彻底废弃 subIdList
        List<WallPost> list = wallPostService.getPostList(userId, categoryIdList, groupList, status, isAnonymous, keyword, currentUserUid);
        PageInfo<WallPost> pageInfo = new PageInfo<>(list);
        return Result.success(pageInfo);
    }







    /**
     * 根据ID查询帖子详情
     * @param id 帖子ID
     * @return 帖子详情
     */
    @GetMapping("/get/{id}")
    public Result get(@PathVariable Long id){
        return Result.success(wallPostService.getPostById(id));
    }

    /**
     * 根据公开帖子编号获取详情，前端不要再使用数据库自增ID。
     */
    @GetMapping("/getByNo/{postNo}")
    public Result getByNo(@PathVariable String postNo) {
        Long postId = wallPostMapper.selectIdByPostNo(postNo);
        if (postId == null) {
            return Result.error("帖子不存在");
        }
        wallPostMapper.increaseViewCount(postNo);
        return Result.success(wallPostService.getPostById(postId));
    }

    /**
     * 浏览量增加接口，列表中曝光或打开帖子时调用。
     */
    @PutMapping("/view/{postNo}")
    public Result view(@PathVariable String postNo, @RequestParam String userUid) {
        int inserted = wallPostMapper.insertAction(postNo, userUid, "VIEW");
        if (inserted > 0) {
            wallPostMapper.increaseViewCount(postNo);
        }
        return Result.success(inserted > 0);
    }

    /**
     * 点赞或取消点赞。liked=true 表示点赞，false 表示取消。
     */
    @PutMapping("/like/{postNo}")
    public Result like(@PathVariable String postNo, @RequestParam String userUid, @RequestParam Boolean liked) {
        int changed = Boolean.TRUE.equals(liked)
                ? wallPostMapper.insertAction(postNo, userUid, "LIKE")
                : wallPostMapper.deleteAction(postNo, userUid, "LIKE");
        if (changed > 0) {
            wallPostMapper.changeLikeCount(postNo, Boolean.TRUE.equals(liked) ? 1 : -1);
            if (Boolean.TRUE.equals(liked)) {
                sendPostInteractionNotice(postNo, userUid, "LIKE", "赞了你的帖子");
            }
        }
        return Result.success(changed > 0);
    }

    /**
     * 收藏或取消收藏。collected=true 表示收藏，false 表示取消。
     */
    @PutMapping("/collect/{postNo}")
    public Result collect(@PathVariable String postNo, @RequestParam String userUid, @RequestParam Boolean collected) {
        int changed = Boolean.TRUE.equals(collected)
                ? wallPostMapper.insertAction(postNo, userUid, "COLLECT")
                : wallPostMapper.deleteAction(postNo, userUid, "COLLECT");
        if (changed > 0) {
            wallPostMapper.changeCollectCount(postNo, Boolean.TRUE.equals(collected) ? 1 : -1);
            if (Boolean.TRUE.equals(collected)) {
                sendPostInteractionNotice(postNo, userUid, "COLLECT", "收藏了你的帖子");
            }
        }
        return Result.success(changed > 0);
    }

    /**
     * 根据UId 查询用户发布过的帖子列表
     */
    @GetMapping("/listByUserId")
    public Result listByUserId(@RequestParam String UID, @RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize){
        return Result.success(wallPostService.getPostListByUserId(UID, pageNum, pageSize));
    }

    /**
     * 根据UID统计用户发布帖子的总数、浏览、点赞、评论。
     */
    @GetMapping("/statsByUserId")
    public Result statsByUserId(@RequestParam String UID) {
        return Result.success(wallPostService.getPostStatsByUserId(UID));
    }

    /**
     * 发布新帖子（限制每日最多100条）
     * @param entity 帖子实体
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody WallPost entity){
        if(!wallPostService.checkUserPostLimit(entity.getUserUid())){
            return Result.error("今日发帖已达上限，最多100条");
        }
        boolean res = wallPostService.addPost(entity);
        return res ? Result.success("发布成功") : Result.error("发布失败");
    }

    /**
     * 修改帖子信息
     * @param entity 帖子实体
     * @return 操作结果
     */
    @PutMapping("/edit")
    public Result edit(@RequestBody WallPost entity){
        boolean res = wallPostService.editPost(entity);
        return res ? Result.success() : Result.error("修改失败");
    }

    /**
     * 逻辑删除帖子
     * @param id 帖子ID
     * @return 操作结果
     */
    @DeleteMapping("/del/{id}")
    public Result del(@PathVariable Long id){
        boolean res = wallPostService.delPost(id);
        return res ? Result.success() : Result.error("删除失败");
    }

    /**
     * 关注或取消关注帖子发布者。followed=true 表示关注，false 表示取消。
     */
    @PutMapping("/follow/{postNo}")
    public Result follow(@PathVariable String postNo, @RequestParam String userUid, @RequestParam Boolean followed) {
        int changed = Boolean.TRUE.equals(followed)
                ? wallPostMapper.insertAction(postNo, userUid, "FOLLOW")
                : wallPostMapper.deleteAction(postNo, userUid, "FOLLOW");
        if (changed > 0 && Boolean.TRUE.equals(followed)) {
            sendPostInteractionNotice(postNo, userUid, "FOLLOW", "关注了你");
        }
        return Result.success(changed > 0);
    }

    /**
     * 向帖子发布者发送互动通知（点赞/收藏/关注）。
     */
    private void sendPostInteractionNotice(String postNo, String actorUid, String actionType, String actionDesc) {
        try {
            Long postId = wallPostMapper.selectIdByPostNo(postNo);
            if (postId == null) return;
            WallPost post = wallPostService.getPostById(postId);
            if (post == null || post.getUserUid() == null) return;
            String ownerUid = post.getUserUid();
            if (ownerUid.equals(actorUid)) return;

            var actor = userService.getUserByUid(actorUid);
            String actorName = actor != null && actor.getUsername() != null ? actor.getUsername() : "同学";

            String content = actorName + " " + actionDesc;
            String postExcerpt = post.getContent() != null && post.getContent().length() > 30
                    ? post.getContent().substring(0, 30) + "..."
                    : (post.getContent() != null ? post.getContent() : "");

            ChatMessage msg = new ChatMessage();
            msg.setConversationId("notice_" + ownerUid);
            msg.setMessageType("system");
            msg.setSenderId("system");
            msg.setSenderName("系统通知");
            msg.setContent(content);
            msg.setSourceType("wall_post");
            msg.setSourceId(postNo);
            msg.setSourceTitle(postExcerpt);
            msg.setTargetUrl("/pages/wall/wall?postNo=" + postNo);
            msg.setConversationTitle("帖子互动");
            chatMessageService.saveMessage(msg);
            chatWebSocketHandler.pushSystemNotice(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}