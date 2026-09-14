package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.mapper.WallCommentMapper;
import com.wwl.mapper.WallPostMapper;
import com.wwl.model.entity.ChatMessage;
import com.wwl.model.entity.WallComment;
import com.wwl.model.entity.WallPost;
import com.wwl.service.ChatMessageService;
import com.wwl.service.IWallCommentService;
import com.wwl.service.IWallPostService;
import com.wwl.service.UserService;
import com.wwl.websocket.handler.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 表白墙评论控制器
 */
@RestController
@RequestMapping("/wall/comment")
public class WallCommentController {

    @Autowired
    private IWallCommentService wallCommentService;

    @Autowired
    private WallPostMapper wallPostMapper;

    @Autowired
    private WallCommentMapper wallCommentMapper;

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private IWallPostService wallPostService;

    @Autowired
    private UserService userService;

    /**
     * 新增评论
     */
    @PostMapping("/add")
    public Result add(@Validated @RequestBody WallComment wallComment) {
        Result result = wallCommentService.addComment(wallComment);
        if (result.getCode() == 1 && wallComment.getPostId() != null) {
            wallPostMapper.changeCommentCount(wallComment.getPostId(), 1);
        }
        return result;
    }

    /**
     * 根据公开帖子编号发表评论，前端无需传数据库帖子ID。
     */
    @PostMapping("/addByPostNo/{postNo}")
    public Result addByPostNo(@PathVariable("postNo") String postNo, @RequestBody WallComment wallComment) {
        Long postId = wallPostMapper.selectIdByPostNo(postNo);
        if (postId == null) {
            return Result.error("帖子不存在");
        }
        wallComment.setPostId(postId);
        Result result = wallCommentService.addComment(wallComment);
        if (result.getCode() == 1) {
            wallPostMapper.changeCommentCount(postId, 1);
            sendCommentNotice(postId, postNo, wallComment.getUserUid(), wallComment.getContent());
        }
        return result;
    }

    /**
     * 获取帖子评论列表
     */
    @GetMapping("/list/{postId}")
    public Result list(@PathVariable("postId") Long postId) {
        List<WallComment> list = wallCommentService.listCommentByPostId(postId);
        return Result.success(list);
    }

    /**
     * 根据公开帖子编号读取评论列表。
     */
    @GetMapping("/listByPostNo/{postNo}")
    public Result listByPostNo(@PathVariable("postNo") String postNo) {
        Long postId = wallPostMapper.selectIdByPostNo(postNo);
        if (postId == null) {
            return Result.error("帖子不存在");
        }
        List<WallComment> list = wallCommentService.listCommentByPostId(postId);
        return Result.success(list);
    }

    /**
     * 软删除评论
     * @param commentId 评论ID
     * @param userUid 当前登录用户uid
     */
    @DeleteMapping("/delete/{commentId}")
    public Result delete(@PathVariable("commentId") Long commentId,
                         @RequestParam("userUid") String userUid) {
        return wallCommentService.deleteComment(commentId,userUid);
    }

    /**
     * 点赞评论（每人只能点赞一次，不支持取消）
     */
    @PutMapping("/like/{commentId}")
    public Result like(@PathVariable("commentId") Long commentId, @RequestParam("userUid") String userUid) {
        int changed = wallCommentMapper.insertLikeAction(commentId, userUid);
        if (changed > 0) {
            wallCommentMapper.changeLikeCount(commentId, 1);
        }
        return Result.success(true);
    }

    /**
     * 向帖子发布者发送评论通知。
     */
    private void sendCommentNotice(Long postId, String postNo, String commenterUid, String commentContent) {
        try {
            WallPost post = wallPostService.getPostById(postId);
            if (post == null || post.getUserUid() == null) return;
            String ownerUid = post.getUserUid();
            if (ownerUid.equals(commenterUid)) return;

            var commenter = userService.getUserByUid(commenterUid);
            String commenterName = commenter != null && commenter.getUsername() != null ? commenter.getUsername() : "同学";

            String excerpt = commentContent != null && commentContent.length() > 30
                    ? commentContent.substring(0, 30) + "..."
                    : (commentContent != null ? commentContent : "");

            ChatMessage msg = new ChatMessage();
            msg.setConversationId("notice_" + ownerUid);
            msg.setMessageType("system");
            msg.setSenderId("system");
            msg.setSenderName("系统通知");
            msg.setContent(commenterName + " 评论了你的帖子：" + excerpt);
            msg.setSourceType("wall_post");
            msg.setSourceId(postNo);
            msg.setSourceTitle("评论通知");
            msg.setTargetUrl("/pages/wall/wall?postNo=" + postNo);
            msg.setConversationTitle("帖子互动");
            chatMessageService.saveMessage(msg);
            chatWebSocketHandler.pushSystemNotice(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}