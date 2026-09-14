package com.wwl.service.impl;

import com.wwl.mapper.ChatMessageMapper;
import com.wwl.mapper.LostFoundMapper;
import com.wwl.model.dto.LostFoundDTO;
import com.wwl.model.entity.ChatMessage;
import com.wwl.model.entity.LostFoundApplication;
import com.wwl.model.entity.LostFoundItem;
import com.wwl.service.LostFoundService;
import com.wwl.websocket.handler.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LostFoundServiceImpl implements LostFoundService {

    @Autowired
    private LostFoundMapper lostFoundMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public List<LostFoundItem> list(LostFoundDTO dto) {
        LostFoundDTO query = dto == null ? new LostFoundDTO() : dto;
        String status = query.getStatus();
        // 处理逗号分隔的status，如 "1,3" 表示已完成（不限用户）
        if (status != null && status.contains(",") && !status.contains("0")) {
            String[] parts = status.split(",");
            if (parts.length == 2) {
                try {
                    LostFoundDTO q1 = new LostFoundDTO();
                    q1.setType(query.getType());
                    q1.setKeyword(query.getKeyword());
                    q1.setStatus(parts[0].trim());
                    q1.setUserUid(query.getUserUid());
                    q1.setPageNum(query.getPageNum());
                    q1.setPageSize(query.getPageSize());
                    LostFoundDTO q2 = new LostFoundDTO();
                    q2.setType(query.getType());
                    q2.setKeyword(query.getKeyword());
                    q2.setStatus(parts[1].trim());
                    q2.setUserUid(query.getUserUid());
                    q2.setPageNum(query.getPageNum());
                    q2.setPageSize(query.getPageSize());
                    List<LostFoundItem> r1 = lostFoundMapper.selectList(q1);
                    List<LostFoundItem> r2 = lostFoundMapper.selectList(q2);
                    java.util.Set<Long> seen = new java.util.HashSet<>();
                    r1.removeIf(item -> !seen.add(item.getId()));
                    r2.removeIf(item -> !seen.add(item.getId()));
                    r1.addAll(r2);
                    r1.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
                    String userUid = query.getUserUid();
                    for (LostFoundItem item : r1) {
                        if (!canViewContact(item, userUid)) {
                            hideContact(item);
                        }
                    }
                    return r1;
                } catch (Exception e) {
                    // fallback to normal query
                }
            }
        }
        List<LostFoundItem> list = lostFoundMapper.selectList(query);
        String userUid = query.getUserUid();
        for (LostFoundItem item : list) {
            if (!canViewContact(item, userUid)) {
                hideContact(item);
            }
        }
        return list;
    }

    @Override
    public LostFoundItem get(Long id, String userUid) {
        lostFoundMapper.increaseViews(id);
        LostFoundItem item = lostFoundMapper.selectById(id, userUid);
        if (item == null) {
            return null;
        }
        if (!canViewContact(item, userUid)) {
            hideContact(item);
        }
        return item;
    }

    @Override
    public LostFoundItem publish(LostFoundDTO dto) {
        if (dto == null || isBlank(dto.getUserUid()) || isBlank(dto.getTitle()) || isBlank(dto.getType())
                || isBlank(dto.getName()) || isBlank(dto.getPhone()) || isBlank(dto.getStudentId())) {
            return null;
        }
        if (!"lost".equals(dto.getType()) && !"found".equals(dto.getType())) {
            return null;
        }

        LostFoundItem item = new LostFoundItem();
        item.setType(dto.getType());
        item.setTitle(dto.getTitle().trim());
        item.setDescription(defaultText(dto.getDescription(), ""));
        item.setLocation(defaultText(dto.getLocation(), ""));
        item.setImage(defaultText(dto.getImage(), ""));
        item.setPublisherUid(dto.getUserUid());
        item.setPublisherName(dto.getName().trim());
        item.setPublisherPhone(dto.getPhone().trim());
        item.setPublisherStudentId(dto.getStudentId().trim());
        item.setStatus(0);
        item.setViews(0);
        item.setIsDeleted(0);
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        return lostFoundMapper.insertItem(item) > 0 ? lostFoundMapper.selectById(item.getId(), null) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LostFoundApplication apply(LostFoundDTO dto) {
        if (dto == null || dto.getItemId() == null || isBlank(dto.getUserUid()) || isBlank(dto.getName())
                || isBlank(dto.getPhone()) || isBlank(dto.getStudentId()) || isBlank(dto.getApplyType())) {
            return null;
        }
        if (!"claim".equals(dto.getApplyType()) && !"return".equals(dto.getApplyType())) {
            return null;
        }
        if ("claim".equals(dto.getApplyType()) && isBlank(dto.getIdCard())) {
            return null;
        }

        LostFoundItem item = lostFoundMapper.selectById(dto.getItemId(), dto.getUserUid());
        if (item == null || item.getStatus() != 0 || dto.getUserUid().equals(item.getPublisherUid())) {
            return null;
        }
        int applyCount = lostFoundMapper.countApplicationsByItemAndApplicant(dto.getItemId(), dto.getUserUid());
        if (applyCount >= 3) {
            return null;
        }
        if (lostFoundMapper.selectOpenApplicationByItem(dto.getItemId()) != null) {
            return null;
        }
        if (lostFoundMapper.selectActiveApplication(dto.getItemId(), dto.getUserUid()) != null) {
            return null;
        }

        LostFoundApplication application = new LostFoundApplication();
        application.setItemId(dto.getItemId());
        application.setApplyType(dto.getApplyType());
        application.setApplicantUid(dto.getUserUid());
        application.setApplicantName(dto.getName().trim());
        application.setApplicantPhone(dto.getPhone().trim());
        application.setApplicantStudentId(dto.getStudentId().trim());
        application.setApplicantIdCard("claim".equals(dto.getApplyType()) ? dto.getIdCard().trim() : null);
        application.setStatus("pending");
        application.setIsDeleted(0);
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());

        int rows = lostFoundMapper.insertApplication(application);
        if (rows <= 0) {
            return null;
        }

        String action = "claim".equals(dto.getApplyType()) ? "申请领取" : "申请归还";
        sendNotice(item.getPublisherUid(), item.getId(), "失物招领通知",
                dto.getName().trim() + action + "《" + item.getTitle() + "》，请进入失物招领处理。");
        return lostFoundMapper.selectApplicationById(application.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(LostFoundDTO dto) {
        LostFoundApplication application = getOperateApplication(dto, "pending");
        if (application == null) {
            return false;
        }
        LostFoundItem item = lostFoundMapper.selectById(application.getItemId(), dto.getUserUid());
        if (item == null || !item.getPublisherUid().equals(dto.getUserUid())) {
            return false;
        }
        boolean ok = lostFoundMapper.updateApplicationStatus(application.getId(), "approved") > 0;
        if (ok) {
            String action = "claim".equals(application.getApplyType()) ? "领取" : "归还";
            sendNotice(application.getApplicantUid(), item.getId(), "申请已同意",
                    item.getPublisherName() + "已同意你的" + action + "申请，可查看对方手机号、姓名和学号。");
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reject(LostFoundDTO dto) {
        LostFoundApplication application = getOperateApplication(dto, "pending");
        if (application == null) {
            return false;
        }
        LostFoundItem item = lostFoundMapper.selectById(application.getItemId(), dto.getUserUid());
        if (item == null || !item.getPublisherUid().equals(dto.getUserUid())) {
            return false;
        }
        boolean ok = lostFoundMapper.updateApplicationStatus(application.getId(), "rejected") > 0;
        if (ok) {
            sendNotice(application.getApplicantUid(), item.getId(), "申请已拒绝",
                    item.getPublisherName() + "拒绝了你的失物招领申请：" + item.getTitle());
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean complete(LostFoundDTO dto) {
        LostFoundApplication application = getOperateApplication(dto, "approved");
        if (application == null) {
            return false;
        }
        LostFoundItem item = lostFoundMapper.selectById(application.getItemId(), dto.getUserUid());
        if (item == null) {
            return false;
        }
        boolean allowed = item.getPublisherUid().equals(dto.getUserUid()) || application.getApplicantUid().equals(dto.getUserUid());
        if (!allowed) {
            return false;
        }

        int newStatus = "claim".equals(application.getApplyType()) ? 3 : 1;

        lostFoundMapper.updateApplicationStatus(application.getId(), "completed");
        lostFoundMapper.resolveItem(application.getItemId(), application.getId(), newStatus, application.getApplicantName());

        String targetUid = item.getPublisherUid().equals(dto.getUserUid()) ? application.getApplicantUid() : item.getPublisherUid();
        String action = "claim".equals(application.getApplyType()) ? "领取" : "归还";
        sendNotice(targetUid, item.getId(), "流程已完成", "物品已确认" + action + "：" + item.getTitle());
        return true;
    }

    @Override
    public Map<String, Object> stats() {
        Map<String, Object> stats = lostFoundMapper.selectStats();
        return stats == null ? new HashMap<>() : stats;
    }

    @Override
    public Map<String, Object> notifications(String userUid) {
        Map<String, Object> result = new HashMap<>();
        if (isBlank(userUid)) {
            result.put("publishedPendingCount", 0);
            result.put("appliedChangedCount", 0);
            return result;
        }
        Map<String, Object> counts = lostFoundMapper.selectNotificationCounts(userUid);
        if (counts == null) {
            result.put("publishedPendingCount", 0);
            result.put("appliedChangedCount", 0);
        } else {
            result.put("publishedPendingCount", counts.get("publishedPendingCount") == null ? 0 : counts.get("publishedPendingCount"));
            result.put("appliedChangedCount", counts.get("appliedChangedCount") == null ? 0 : counts.get("appliedChangedCount"));
        }
        return result;
    }

    private LostFoundApplication getOperateApplication(LostFoundDTO dto, String requiredStatus) {
        if (dto == null || dto.getApplicationId() == null || isBlank(dto.getUserUid())) {
            return null;
        }
        LostFoundApplication application = lostFoundMapper.selectApplicationById(dto.getApplicationId());
        if (application == null || !requiredStatus.equals(application.getStatus())) {
            return null;
        }
        return application;
    }

    private boolean canViewContact(LostFoundItem item, String userUid) {
        if (item == null || isBlank(userUid)) {
            return false;
        }
        if (userUid.equals(item.getPublisherUid())) {
            return true;
        }
        return userUid.equals(item.getApplicantUid())
                && ("approved".equals(item.getApplicationStatus()) || "completed".equals(item.getApplicationStatus()));
    }

    private void hideContact(LostFoundItem item) {
        if (item == null) {
            return;
        }
        item.setPublisherPhone(null);
        item.setPublisherStudentId(null);
        item.setApplicantPhone(null);
        item.setApplicantStudentId(null);
        item.setApplicantIdCard(null);
    }

    private void sendNotice(String userUid, Long itemId, String title, String content) {
        if (isBlank(userUid)) {
            return;
        }
        ChatMessage msg = new ChatMessage();
        msg.setConversationId("notice_" + userUid);
        msg.setSourceType("system");
        msg.setSourceId(String.valueOf(itemId));
        msg.setConversationTitle(title);
        msg.setConversationAvatar("/static/default-avatar.png");
        msg.setSenderId("system");
        msg.setSenderName("系统通知");
        msg.setSenderAvatar("/static/default-avatar.png");
        msg.setContent(content);
        msg.setMessageType("system");
        msg.setIsDeleted(0);
        msg.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(msg);
        chatWebSocketHandler.pushSystemNotice(msg);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultText(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value.trim();
    }

    @Override
    public boolean delete(Long id, String userUid) {
        if (id == null || isBlank(userUid)) {
            return false;
        }
        return lostFoundMapper.softDeleteItem(id, userUid) > 0;
    }
}