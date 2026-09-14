package com.wwl.service.impl;

import com.wwl.mapper.OrganizationActivityMapper;
import com.wwl.model.dto.OrganizationActivityDTO;
import com.wwl.model.entity.OrganizationActivity;
import com.wwl.model.entity.OrganizationActivityMember;
import com.wwl.service.OrganizationActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrganizationActivityServiceImpl implements OrganizationActivityService {

    private static final String DEFAULT_POSTER = "/static/organization/feature-card.jpg";
    private static final String DEFAULT_AVATAR = "/static/default-avatar.png";
    private static final String DEFAULT_QRCODE = "/static/11/1.png";

    @Autowired
    private OrganizationActivityMapper organizationActivityMapper;

    @Override
    public List<Map<String, Object>> getCategories() {
        List<Map<String, Object>> list = new ArrayList<>();
        addCategory(list, "体育运动", 1, "篮球", "足球", "羽毛球", "乒乓球", "排球", "网球", "台球", "飞盘", "游泳", "跑步", "骑行", "徒步", "露营", "健身", "瑜伽", "跳舞", "武术搏击", "滑板轮滑", "体育训练", "运动搭子");
        addCategory(list, "学业备考", 21, "自习", "考研", "考公", "英语学习", "考证备考", "期末复习", "专业课学习", "小组作业", "论文互助", "毕设组队", "编程开发", "设计剪辑", "办公软件", "口语练习", "阅读分享", "技能学习", "学科竞赛", "创业交流", "学习监督", "学习搭子");
        addCategory(list, "美食觅食", 41, "火锅", "烤肉", "烧烤", "聚餐干饭", "探店", "夜宵", "小吃", "早餐", "午饭", "晚饭", "奶茶", "咖啡", "甜品", "自助餐", "食堂搭子", "轻食减脂餐", "野餐", "节日聚餐", "拼桌约饭", "美食分享");
        addCategory(list, "休闲玩乐", 61, "桌游", "剧本杀", "密室逃脱", "狼人杀", "麻将", "扑克", "线下牌局", "电竞开黑", "手游组队", "端游组队", "联机游戏", "网吧开黑", "KTV", "看电影", "DIY手作", "漫展", "音乐节", "演唱会", "宠物互动", "娱乐聚会");
        addCategory(list, "社交搭伴", 81, "逛街", "散步", "城市漫步", "网红打卡", "书店", "图书馆", "看展", "摄影", "志愿活动", "社团活动", "球赛观战", "校园活动", "新生交友", "同专业交流", "兴趣交友", "脱单交友", "闲聊唠嗑", "周边出游", "临时组局", "找搭子");
        return list;
    }

    @Override
    public List<OrganizationActivity> list(OrganizationActivityDTO dto) {
        if (dto.getStatus() == null || dto.getStatus().trim().isEmpty()) {
            dto.setStatus("active");
        }
        List<OrganizationActivity> activities = organizationActivityMapper.selectActivityList(dto);
        fillAvatarList(activities);
        return activities;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationActivity publish(OrganizationActivityDTO dto) {
        if (dto == null || isBlank(dto.getTitle()) || isBlank(dto.getActivityTime()) || isBlank(dto.getAddress())) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        OrganizationActivity activity = new OrganizationActivity();
        activity.setTitle(dto.getTitle());
        activity.setCategory(defaultText(dto.getCategory(), "休闲玩乐"));
        activity.setTagId(dto.getTagId());
        activity.setTagName(defaultText(dto.getTagName(), "电竞开黑"));
        activity.setActivityTime(dto.getActivityTime());
        activity.setAddress(dto.getAddress());
        activity.setMaxPeople(dto.getMaxPeople() == null || dto.getMaxPeople() < 2 ? 2 : dto.getMaxPeople());
        activity.setJoinNum(0);
        activity.setGenderLimit(defaultText(dto.getGenderLimit(), "不限"));
        activity.setFeeType(defaultText(dto.getFeeType(), "免费"));
        activity.setDepositRequired(Boolean.TRUE.equals(dto.getDepositRequired()));
        activity.setDepositAmount(activity.getDepositRequired() ? defaultMoney(dto.getDepositAmount()) : BigDecimal.ZERO);
        activity.setHostDesc(defaultText(dto.getHostDesc(), ""));
        activity.setPosterImg(defaultImage(dto.getPosterImg(), DEFAULT_POSTER));
        activity.setBrandName(defaultText(dto.getBrandName(), defaultText(dto.getNickname(), "校园搭子")));
        activity.setBrandAvatar(defaultImage(dto.getBrandAvatar(), defaultImage(dto.getAvatar(), DEFAULT_AVATAR)));
        activity.setQrcodeUrl(defaultImage(dto.getQrcodeUrl(), DEFAULT_QRCODE));
        activity.setCreatorUserId(defaultText(dto.getUserId(), "system"));
        int groupCount = organizationActivityMapper.countByCreatorOrBrand(activity.getCreatorUserId(), activity.getBrandName()) + 1;
        activity.setGroupCount(groupCount);
        activity.setStatus("active");
        activity.setIsDeleted(0);
        activity.setCreateTime(now);
        activity.setUpdateTime(now);
        int rows = organizationActivityMapper.insertActivity(activity);
        if (rows <= 0) {
            return null;
        }
        organizationActivityMapper.syncGroupCountByCreatorOrBrand(activity.getCreatorUserId(), activity.getBrandName());
        return organizationActivityMapper.selectById(activity.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationActivity join(OrganizationActivityDTO dto) {
        if (dto == null || dto.getActivityId() == null || isBlank(dto.getUserId())) {
            return null;
        }
        OrganizationActivity oldActivity = organizationActivityMapper.selectById(dto.getActivityId());
        if (oldActivity == null || !"active".equals(oldActivity.getStatus())) {
            return null;
        }
        OrganizationActivityMember oldMember = organizationActivityMapper.selectMember(dto.getActivityId(), dto.getUserId());
        if (oldMember != null && "joined".equals(oldMember.getStatus())) {
            return withAvatarList(organizationActivityMapper.selectById(dto.getActivityId()));
        }

        int increased = organizationActivityMapper.increaseJoinNum(dto.getActivityId());
        if (increased <= 0) {
            return null;
        }

        if (oldMember == null) {
            OrganizationActivityMember member = new OrganizationActivityMember();
            member.setActivityId(dto.getActivityId());
            member.setUserId(dto.getUserId());
            member.setNickname(defaultText(dto.getNickname(), "报名同学"));
            member.setAvatar(defaultImage(dto.getAvatar(), DEFAULT_AVATAR));
            member.setStatus("joined");
            member.setCreateTime(LocalDateTime.now());
            member.setUpdateTime(LocalDateTime.now());
            organizationActivityMapper.insertMember(member);
        } else {
            organizationActivityMapper.updateMemberStatus(dto.getActivityId(), dto.getUserId(), "joined");
        }
        return withAvatarList(organizationActivityMapper.selectById(dto.getActivityId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quit(OrganizationActivityDTO dto) {
        if (dto == null || dto.getActivityId() == null || isBlank(dto.getUserId())) {
            return false;
        }
        OrganizationActivityMember member = organizationActivityMapper.selectMember(dto.getActivityId(), dto.getUserId());
        if (member == null || !"joined".equals(member.getStatus())) {
            return false;
        }
        int rows = organizationActivityMapper.updateMemberStatus(dto.getActivityId(), dto.getUserId(), "cancelled");
        if (rows > 0) {
            organizationActivityMapper.decreaseJoinNum(dto.getActivityId());
        }
        return rows > 0;
    }

    @Override
    public boolean updateStatus(OrganizationActivityDTO dto) {
        if (dto == null || dto.getId() == null || isBlank(dto.getStatus())) {
            return false;
        }
        OrganizationActivity activity = new OrganizationActivity();
        activity.setId(dto.getId());
        activity.setStatus(dto.getStatus());
        activity.setUpdateTime(LocalDateTime.now());
        return organizationActivityMapper.updateActivity(activity) > 0;
    }

    @Override
    public Map<String, Object> mine(String userId) {
        Map<String, Object> data = new HashMap<>();
        if (isBlank(userId)) {
            data.put("created", new ArrayList<>());
            data.put("joined", new ArrayList<>());
            return data;
        }
        List<OrganizationActivity> created = organizationActivityMapper.selectCreatedByUser(userId);
        List<OrganizationActivity> joined = organizationActivityMapper.selectJoinedByUser(userId);
        fillAvatarList(created);
        fillAvatarList(joined);
        data.put("created", created);
        data.put("joined", joined);
        return data;
    }

    @Override
    public OrganizationActivity getById(Long id) {
        return withAvatarList(organizationActivityMapper.selectById(id));
    }

    @Override
    public List<OrganizationActivityMember> getMembers(Long activityId) {
        return organizationActivityMapper.selectMembersByActivityId(activityId);
    }

    private void fillAvatarList(List<OrganizationActivity> activities) {
        if (activities == null) {
            return;
        }
        for (OrganizationActivity activity : activities) {
            withAvatarList(activity);
        }
    }

    private OrganizationActivity withAvatarList(OrganizationActivity activity) {
        if (activity != null && activity.getId() != null) {
            activity.setAvatarList(organizationActivityMapper.selectAvatarList(activity.getId()));
        }
        return activity;
    }

    private void addCategory(List<Map<String, Object>> list, String name, int start, String... tags) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("start", start);
        item.put("tags", tags);
        list.add(item);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultText(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value;
    }

    private String defaultImage(String value, String defaultValue) {
        if (isBlank(value) || value.trim().startsWith("blob:")) {
            return defaultValue;
        }
        return value;
    }

    private BigDecimal defaultMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}