package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.mapper.OrganizationActivityMapper;
import com.wwl.model.dto.OrganizationActivityDTO;
import com.wwl.model.entity.ChatMessage;
import com.wwl.model.entity.OrganizationActivity;
import com.wwl.model.entity.OrganizationActivityMember;
import com.wwl.service.ChatMessageService;
import com.wwl.service.OrganizationActivityService;
import com.wwl.service.UserService;
import com.wwl.websocket.handler.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organization")
public class OrganizationActivityController {
    @Autowired
    private OrganizationActivityService organizationActivityService;

    @Autowired
    private OrganizationActivityMapper organizationActivityMapper;

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private UserService userService;

    @GetMapping("/categories")
    public Result categories() {
        return Result.success(organizationActivityService.getCategories());
    }

    @PostMapping("/list")
    public Result list(@RequestBody(required = false) OrganizationActivityDTO dto) {
        return Result.success(organizationActivityService.list(dto == null ? new OrganizationActivityDTO() : dto));
    }

    @PostMapping("/publish")
    public Result publish(@RequestBody OrganizationActivityDTO dto) {
        OrganizationActivity activity = organizationActivityService.publish(dto);
        return activity == null ? Result.error("发布失败") : Result.success(activity);
    }

    @PostMapping("/join")
    public Result join(@RequestBody OrganizationActivityDTO dto) {
        OrganizationActivity activity = organizationActivityService.join(dto);
        if (activity == null) {
            return Result.error("报名失败或人数已满");
        }
        sendJoinNotice(activity, dto.getUserId(), dto.getNickname());
        return Result.success(activity);
    }

    @PostMapping("/quit")
    public Result quit(@RequestBody OrganizationActivityDTO dto) {
        return organizationActivityService.quit(dto) ? Result.success("取消报名成功") : Result.error("取消报名失败");
    }

    @PostMapping("/status")
    public Result status(@RequestBody OrganizationActivityDTO dto) {
        return organizationActivityService.updateStatus(dto) ? Result.success("操作成功") : Result.error("操作失败");
    }

    @GetMapping("/my")
    public Result mine(@RequestParam("userId") String userId) {
        return Result.success(organizationActivityService.mine(userId));
    }

    @GetMapping("/get/{id}")
    public Result getById(@PathVariable("id") Long id) {
        OrganizationActivity activity = organizationActivityService.getById(id);
        return activity == null ? Result.error("组局不存在") : Result.success(activity);
    }

    @GetMapping("/members/{activityId}")
    public Result members(@PathVariable("activityId") Long activityId) {
        return Result.success(organizationActivityService.getMembers(activityId));
    }

    /**
     * 向组局创建者发送报名通知。
     */
    private void sendJoinNotice(OrganizationActivity activity, String joinerUid, String joinerNickname) {
        try {
            if (activity == null || activity.getCreatorUserId() == null) return;
            String creatorUid = activity.getCreatorUserId();
            if (creatorUid.equals(joinerUid)) return;

            String joinerName = joinerNickname != null ? joinerNickname : "同学";

            ChatMessage msg = new ChatMessage();
            msg.setConversationId("notice_" + creatorUid);
            msg.setMessageType("system");
            msg.setSenderId("system");
            msg.setSenderName("系统通知");
            msg.setContent(joinerName + " 报名了你的组局\"" + activity.getTitle() + "\"");
            msg.setSourceType("organization");
            msg.setSourceId(String.valueOf(activity.getId()));
            msg.setSourceTitle(activity.getTitle());
            msg.setTargetUrl("/pages/organization/organizationDetail?activityId=" + activity.getId());
            msg.setConversationTitle("组局通知");
            chatMessageService.saveMessage(msg);
            chatWebSocketHandler.pushSystemNotice(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}