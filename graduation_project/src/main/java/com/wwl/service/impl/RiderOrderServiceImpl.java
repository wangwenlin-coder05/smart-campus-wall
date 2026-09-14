package com.wwl.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wwl.common.enums.OrderStatusEnum;
import com.wwl.common.util.OrderNoGenerator;
import com.wwl.mapper.OrderImageMapper;
import com.wwl.mapper.OrderInfoMapper;
import com.wwl.mapper.RiderOrderMapper;
import com.wwl.mapper.UserMapper;
import com.wwl.model.dto.OrderAcceptDTO;
import com.wwl.model.dto.OrderDisputeDTO;
import com.wwl.model.dto.RiderOrderQueryDTO;
import com.wwl.model.entity.ChatMessage;
import com.wwl.model.entity.OrderImage;
import com.wwl.model.entity.OrderInfo;
import com.wwl.model.entity.RiderOrder;
import com.wwl.model.vo.OrderCountVO;
import com.wwl.model.vo.RiderOwnOrderVO;
import com.wwl.service.RiderOrderService;
import com.wwl.websocket.handler.ChatWebSocketHandler;
import com.wwl.websocket.handler.DisputeCountWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RiderOrderServiceImpl implements RiderOrderService {
    private static final DateTimeFormatter LOG_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private RiderOrderMapper riderOrderMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderImageMapper orderImageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptOrder(OrderAcceptDTO dto) {
        if (dto == null || isBlank(dto.getOrderNo()) || isBlank(dto.getRiderUserId())) {
            return false;
        }
        OrderInfo oldOrder = orderInfoMapper.selectByOrderNo(dto.getOrderNo());
        if (oldOrder == null || !OrderStatusEnum.WAIT_ACCEPT.getCode().equals(oldOrder.getOrderStatus())) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        RiderOrder riderOrder = new RiderOrder();
        BeanUtils.copyProperties(dto, riderOrder);
        riderOrder.setRiderOrderNo(OrderNoGenerator.generateWithPrefix("RID"))
                .setAcceptTime(now)
                .setRiderStatus(OrderStatusEnum.ACCEPTED.getCode())
                .setCreateTime(now)
                .setUpdateTime(now)
                .setIsDeleted(0);

        int insertRows = riderOrderMapper.insertRiderOrder(riderOrder);
        if (insertRows <= 0) {
            return false;
        }

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderNo(dto.getOrderNo());
        orderInfo.setRiderId(parseLong(dto.getRiderUserId()));
        orderInfo.setOrderStatus(OrderStatusEnum.ACCEPTED.getCode());
        orderInfo.setPreOrderStatus(oldOrder.getOrderStatus());
        orderInfo.setUpdateTime(now);
        orderInfo.setLifecycleLog(appendLifecycleLog(
                oldOrder.getLifecycleLog(),
                "骑手接单",
                "骑手接下订单，开始准备配送",
                now
        ));
        boolean updated = orderInfoMapper.updateOrder(orderInfo) > 0;
        if (updated) {
            pushOrderStatusNotice(oldOrder.getUserId(), oldOrder.getOrderNo(),
                    "订单已被接单", "您的订单已被骑手接单，骑手正在赶来，请保持手机畅通");
        }
        return updated;
    }

    @Override
    public PageInfo<RiderOwnOrderVO> riderGetOwnOrderList(RiderOrderQueryDTO dto) {
        PageHelper.startPage(dto.getPageNum(), dto.getPageSize());
        List<RiderOwnOrderVO> list = riderOrderMapper.selectRiderSelfOrderPage(dto);
        PageInfo<RiderOwnOrderVO> page = new PageInfo<>(list);
        if (list == null || list.isEmpty()) {
            return page;
        }

        List<Long> orderIdList = list.stream()
                .map(RiderOwnOrderVO::getId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        List<OrderImage> allImage = orderImageMapper.selectByOrderIdList(orderIdList);
        Map<Long, List<String>> imageMap = allImage.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        OrderImage::getOrderId,
                        java.util.stream.Collectors.mapping(OrderImage::getImageUrl, java.util.stream.Collectors.toList())
                ));

        list.forEach(vo -> {
            vo.setStatusName(OrderStatusEnum.getDescByCode(vo.getOrderStatus()));
            vo.setOrderImageList(imageMap.getOrDefault(vo.getId(), new ArrayList<>()));
        });
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderStatus(OrderAcceptDTO dto) {
        if (dto == null || isBlank(dto.getOrderNo()) || dto.getOrderStatus() == null) {
            return false;
        }
        OrderInfo oldOrder = orderInfoMapper.selectByOrderNo(dto.getOrderNo());
        if (oldOrder == null) {
            return false;
        }

        Integer status = dto.getOrderStatus();
        LocalDateTime now = LocalDateTime.now();
        RiderOrder riderOrder = new RiderOrder();
        BeanUtils.copyProperties(dto, riderOrder);
        riderOrder.setRiderStatus(status);
        riderOrder.setUpdateTime(now);
        if (OrderStatusEnum.ACCEPTED.getCode().equals(status)) {
            riderOrder.setAcceptTime(now);
        } else if (OrderStatusEnum.DELIVERING.getCode().equals(status)) {
            riderOrder.setPickTime(now);
        } else if (OrderStatusEnum.ARRIVED.getCode().equals(status)) {
            riderOrder.setDeliverTime(now);
        } else if (OrderStatusEnum.FINISHED.getCode().equals(status)) {
            riderOrder.setFinishTime(now);
        }

        int updateRows = riderOrderMapper.updateRiderOrder(riderOrder);
        if (updateRows <= 0) {
            return false;
        }

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderNo(dto.getOrderNo());
        orderInfo.setOrderStatus(status);
        orderInfo.setPreOrderStatus(oldOrder.getOrderStatus());
        orderInfo.setUpdateTime(now);
        orderInfo.setLifecycleLog(appendLifecycleLog(
                oldOrder.getLifecycleLog(),
                getStatusLogTitle(status),
                getStatusLogDesc(status),
                now
        ));
        boolean updated = orderInfoMapper.updateOrder(orderInfo) > 0;
        if (updated) {
            if (OrderStatusEnum.DELIVERING.getCode().equals(status)) {
                pushOrderStatusNotice(oldOrder.getUserId(), oldOrder.getOrderNo(),
                        "骑手已取件", "骑手已取到商品/物品，正在配送中，预计很快送达");
            } else if (OrderStatusEnum.ARRIVED.getCode().equals(status)) {
                pushOrderStatusNotice(oldOrder.getUserId(), oldOrder.getOrderNo(),
                        "订单已送达", "骑手已将物品送达，请确认收货");
            }
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disputeRiderOrder(OrderDisputeDTO dto) {
        if (dto == null || isBlank(dto.getOrderNo()) || dto.getDisposeType() == null || isBlank(dto.getReason())) {
            return false;
        }
        OrderInfo order = orderInfoMapper.selectByOrderNo(dto.getOrderNo());
        if (order == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderNo(dto.getOrderNo());
        orderInfo.setUpdateTime(now);

        RiderOrder riderOrder = new RiderOrder();
        riderOrder.setOrderNo(dto.getOrderNo());
        riderOrder.setUpdateTime(now);
        riderOrder.setCancelReason(dto.getReason());

        if (Integer.valueOf(1).equals(dto.getDisposeType())) {
            orderInfo.setOrderStatus(OrderStatusEnum.CANCELLED.getCode());
            orderInfo.setLifecycleLog(appendLifecycleLog(
                    order.getLifecycleLog(),
                    "骑手同意取消",
                    "骑手同意客户取消订单，理由：" + dto.getReason(),
                    now
            ));
            riderOrder.setRiderStatus(OrderStatusEnum.CANCELLED.getCode());
        } else if (Integer.valueOf(2).equals(dto.getDisposeType())) {
            Integer restoreStatus = order.getPreOrderStatus();
            if (restoreStatus == null || OrderStatusEnum.DISPUTE.getCode().equals(restoreStatus)) {
                restoreStatus = OrderStatusEnum.ACCEPTED.getCode();
            }
            orderInfo.setOrderStatus(restoreStatus);
            orderInfo.setLifecycleLog(appendLifecycleLog(
                    order.getLifecycleLog(),
                    "骑手拒绝取消",
                    "骑手拒绝客户取消申请，理由：" + dto.getReason(),
                    now
            ));
            riderOrder.setRiderStatus(restoreStatus);
        } else {
            return false;
        }

        int o = orderInfoMapper.updateOrder(orderInfo);
        int r = riderOrderMapper.updateRiderOrder(riderOrder);
        if (o > 0 && r > 0) {
            if (Integer.valueOf(1).equals(dto.getDisposeType())) {
                pushOrderStatusNotice(order.getUserId(), order.getOrderNo(),
                        "骑手同意取消", "骑手已同意取消订单，理由：" + dto.getReason());
            } else if (Integer.valueOf(2).equals(dto.getDisposeType())) {
                pushOrderStatusNotice(order.getUserId(), order.getOrderNo(),
                        "骑手拒绝取消", "骑手拒绝了取消申请，理由：" + dto.getReason() + "，订单继续配送");
            }
        }
        if (o > 0 && r > 0 && !isBlank(dto.getRiderUserId())) {
            int count = riderOrderMapper.countDisputeByRiderUserId(dto.getRiderUserId());
            DisputeCountWebSocketHandler.pushDisputeCountToRider(dto.getRiderUserId(), count);
        }
        return o > 0 && r > 0;
    }

    @Override
    public OrderCountVO getAllStatusCount(String riderUserId) {
        return riderOrderMapper.countAllStatus(riderUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean riderCancelOrder(OrderAcceptDTO dto) {
        if (dto == null || isBlank(dto.getOrderNo()) || isBlank(dto.getCancelReason())) {
            return false;
        }
        OrderInfo order = orderInfoMapper.selectByOrderNo(dto.getOrderNo());
        if (order == null) {
            return false;
        }
        Integer status = order.getOrderStatus();
        if (!OrderStatusEnum.ACCEPTED.getCode().equals(status)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        RiderOrder riderOrder = new RiderOrder();
        riderOrder.setOrderNo(dto.getOrderNo());
        riderOrder.setRiderStatus(OrderStatusEnum.CANCELLED.getCode());
        riderOrder.setCancelReason(dto.getCancelReason());
        riderOrder.setUpdateTime(now);
        riderOrder.setIsDeleted(1);
        int riderRows = riderOrderMapper.updateRiderOrder(riderOrder);

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderNo(dto.getOrderNo());
        orderInfo.setRiderId(0L);
        orderInfo.setOrderStatus(OrderStatusEnum.WAIT_ACCEPT.getCode());
        orderInfo.setPreOrderStatus(status);
        orderInfo.setUpdateTime(now);
        orderInfo.setLifecycleLog(appendLifecycleLog(
                order.getLifecycleLog(),
                "骑手主动取消订单",
                "骑手主动取消订单，理由：" + dto.getCancelReason() + "；订单重新流入接单大厅",
                now
        ));
        int orderRows = orderInfoMapper.updateOrder(orderInfo);
        if (riderRows > 0 && orderRows > 0) {
            pushOrderStatusNotice(order.getUserId(), order.getOrderNo(),
                    "骑手取消订单", "骑手已主动取消订单，理由：" + dto.getCancelReason() + "，订单已重新回到接单大厅");
        }
        return riderRows > 0 && orderRows > 0;
    }

    @Override
    public int addRiderOrder(RiderOrder riderOrder) {
        riderOrder.setIsDeleted(0);
        riderOrder.setCreateTime(LocalDateTime.now());
        riderOrder.setUpdateTime(LocalDateTime.now());
        return riderOrderMapper.insertRiderOrder(riderOrder);
    }

    private String getStatusLogTitle(Integer status) {
        if (OrderStatusEnum.DELIVERING.getCode().equals(status)) {
            return "骑手已取件";
        }
        if (OrderStatusEnum.ARRIVED.getCode().equals(status)) {
            return "骑手已送达";
        }
        if (OrderStatusEnum.FINISHED.getCode().equals(status)) {
            return "订单完成";
        }
        return "订单状态更新";
    }

    private String getStatusLogDesc(Integer status) {
        if (OrderStatusEnum.DELIVERING.getCode().equals(status)) {
            return "骑手已取到商品/物品，订单进入配送中";
        }
        if (OrderStatusEnum.ARRIVED.getCode().equals(status)) {
            return "骑手确认已送达，等待客户确认收货";
        }
        if (OrderStatusEnum.FINISHED.getCode().equals(status)) {
            return "订单已完成";
        }
        return "订单状态变更为：" + OrderStatusEnum.getDescByCode(status);
    }

    private String appendLifecycleLog(String oldLog, String title, String desc, LocalDateTime time) {
        String entry = LOG_TIME_FORMATTER.format(time) + "|" + sanitizeLogText(title) + "|" + sanitizeLogText(desc);
        if (oldLog == null || oldLog.trim().isEmpty()) {
            return entry;
        }
        return oldLog + "\n" + entry;
    }

    private String sanitizeLogText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("|", " ").replace("\r", " ").replace("\n", " ");
    }

    private Long parseLong(String value) {
        try {
            return value == null ? null : Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void pushOrderStatusNotice(Long creatorUserId, String orderNo, String title, String content) {
        if (creatorUserId == null || isBlank(orderNo)) {
            return;
        }
        try {
            String userUid = userMapper.selectUidById(creatorUserId);
            if (isBlank(userUid)) {
                log.warn("未找到用户uid, userId={}", creatorUserId);
                return;
            }
            ChatMessage msg = new ChatMessage();
            msg.setConversationId("notice_" + userUid);
            msg.setSourceType("order");
            msg.setSourceId(orderNo);
            msg.setConversationTitle(title);
            msg.setConversationAvatar("/static/default-avatar.png");
            msg.setSenderId("system");
            msg.setSenderName("系统通知");
            msg.setSenderAvatar("/static/default-avatar.png");
            msg.setContent(content);
            msg.setMessageType("order_status");
            msg.setIsDeleted(0);
            msg.setCreateTime(LocalDateTime.now());
            chatWebSocketHandler.pushSystemNotice(msg);
        } catch (Exception e) {
            log.error("推送订单状态通知失败, userId={}, orderNo={}, err={}", creatorUserId, orderNo, e.getMessage());
        }
    }
}