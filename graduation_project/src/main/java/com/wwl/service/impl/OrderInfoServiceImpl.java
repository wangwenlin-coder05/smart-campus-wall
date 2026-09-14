package com.wwl.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wwl.common.enums.OrderStatusEnum;
import com.wwl.common.util.OrderNoGenerator;
import com.wwl.mapper.OrderImageMapper;
import com.wwl.mapper.OrderInfoMapper;
import com.wwl.mapper.RiderOrderMapper;
import com.wwl.mapper.UserMapper;
import com.wwl.model.dto.CancelOrderDTO;
import com.wwl.model.dto.OrderInfoDTO;
import com.wwl.model.dto.OrderQueryDTO;
import com.wwl.model.entity.ChatMessage;
import com.wwl.model.entity.OrderImage;
import com.wwl.model.entity.OrderInfo;
import com.wwl.model.entity.RiderOrder;
import com.wwl.model.vo.OrderHallVO;
import com.wwl.model.vo.OrderInfoVO;
import com.wwl.service.OrderInfoService;
import com.wwl.websocket.handler.ChatWebSocketHandler;
import com.wwl.websocket.handler.DisputeCountWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderInfoServiceImpl implements OrderInfoService {
    private static final int MAX_CANCEL_APPLY_COUNT = 5;
    private static final DateTimeFormatter LOG_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderImageMapper orderImageMapper;

    @Autowired
    private RiderOrderMapper riderOrderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addOrderData(OrderInfoDTO orderInfoDTO) {
        OrderInfo orderInfo = new OrderInfo();
        BeanUtils.copyProperties(orderInfoDTO, orderInfo);
        prepareNewOrder(orderInfo);
        int rows = orderInfoMapper.insertOrder(orderInfo);
        if (rows <= 0) {
            return false;
        }
        saveOrderImages(orderInfo.getId(), orderInfoDTO.getOrderImageList());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addOrder(OrderInfo orderInfo, List<String> imgUrlList) {
        prepareNewOrder(orderInfo);
        int rows = orderInfoMapper.insertOrder(orderInfo);
        if (rows <= 0) {
            return false;
        }
        saveOrderImages(orderInfo.getId(), imgUrlList);
        return true;
    }

    @Override
    public OrderInfoVO getOrderById(Long id) {
        OrderInfoVO orderInfoVO = orderInfoMapper.getOrderByIdWithAddress(id);
        if (orderInfoVO != null) {
            List<OrderImage> images = orderImageMapper.selectByOrderId(id);
            if (images != null && !images.isEmpty()) {
                orderInfoVO.setOrderImageList(images.stream().map(OrderImage::getImageUrl).collect(Collectors.toList()));
            }
        }
        return orderInfoVO;
    }

    @Override
    public PageInfo<OrderHallVO> getHallOrderList(OrderQueryDTO query) {
        normalizeHallParentType(query);
        Long pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1L : query.getPageNum();
        Long pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 20L : query.getPageSize();
        PageHelper.startPage(pageNum.intValue(), pageSize.intValue());
        return new PageInfo<>(orderInfoMapper.selectHallOrderList(query));
    }

    @Override
    public List<OrderInfoVO> getUserOrderList(Long userId) {
        List<OrderInfoVO> orderList = orderInfoMapper.getOrderByUserIdWithAddress(userId);
        fillOrderImages(orderList);
        return orderList;
    }

    @Override
    public PageInfo<OrderInfoVO> getUserOrderPage(Long userId, Long pageNum, Long pageSize) {
        long currentPage = pageNum == null || pageNum < 1 ? 1 : pageNum;
        long currentSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        PageHelper.startPage((int) currentPage, (int) currentSize);
        List<OrderInfoVO> list = orderInfoMapper.getOrderByUserIdWithAddress(userId);
        fillOrderImages(list);
        return new PageInfo<>(list);
    }

    @Override
    public List<OrderInfoVO> orderConditionList(OrderInfoDTO orderInfoDTO) {
        List<OrderInfoVO> orderList = orderInfoMapper.getOrderByConditionWithAddress(orderInfoDTO);
        fillOrderImages(orderList);
        return orderList;
    }

    @Override
    public boolean updateOrder(OrderInfo orderInfo) {
        return orderInfoMapper.updateOrder(orderInfo) > 0;
    }

    @Override
    public boolean deleteOrder(Long id) {
        return orderInfoMapper.deleteOrder(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(CancelOrderDTO cancelOrderDTO) {
        if (cancelOrderDTO == null || isBlank(cancelOrderDTO.getOrderNo()) || isBlank(cancelOrderDTO.getCancelReason())) {
            return false;
        }
        OrderInfo order = orderInfoMapper.selectByOrderNo(cancelOrderDTO.getOrderNo());
        if (order == null) {
            return false;
        }
        Integer currStatus = order.getOrderStatus();
        if (OrderStatusEnum.DISPUTE.getCode().equals(currStatus) || OrderStatusEnum.CANCELLED.getCode().equals(currStatus)
                || OrderStatusEnum.FINISHED.getCode().equals(currStatus)) {
            return false;
        }

        int cancelApplyCount = order.getCancelApplyCount() == null ? 0 : order.getCancelApplyCount();
        if (cancelApplyCount >= MAX_CANCEL_APPLY_COUNT) {
            throw new IllegalStateException("客户最多只能申请取消 5 次");
        }

        LocalDateTime now = LocalDateTime.now();
        OrderInfo update = new OrderInfo();
        update.setOrderNo(cancelOrderDTO.getOrderNo());
        update.setCancelReason(cancelOrderDTO.getCancelReason());
        update.setCancelApplyCount(cancelApplyCount + 1);
        update.setUpdateTime(now);
        update.setLifecycleLog(appendLifecycleLog(
                order.getLifecycleLog(),
                "客户申请取消订单",
                "客户第 " + (cancelApplyCount + 1) + " 次申请取消订单，理由：" + cancelOrderDTO.getCancelReason(),
                now
        ));

        if (OrderStatusEnum.WAIT_ACCEPT.getCode().equals(currStatus)) {
            update.setOrderStatus(OrderStatusEnum.CANCELLED.getCode());
            return orderInfoMapper.updateOrder(update) > 0;
        }

        update.setPreOrderStatus(currStatus);
        update.setOrderStatus(OrderStatusEnum.DISPUTE.getCode());
        int orderRows = orderInfoMapper.updateOrder(update);

        RiderOrder riderOrder = new RiderOrder();
        riderOrder.setOrderNo(cancelOrderDTO.getOrderNo());
        riderOrder.setPreDeliveryStatus(currStatus);
        riderOrder.setRiderStatus(OrderStatusEnum.DISPUTE.getCode());
        riderOrder.setUpdateTime(now);
        int riderRows = riderOrderMapper.updateRiderOrder(riderOrder);

        if (orderRows > 0 && riderRows > 0) {
            RiderOrder existing = riderOrderMapper.selectByOrderNo(cancelOrderDTO.getOrderNo());
            if (existing != null && !isBlank(existing.getRiderUserId())) {
                int count = riderOrderMapper.countDisputeByRiderUserId(existing.getRiderUserId());
                DisputeCountWebSocketHandler.pushDisputeCountToRider(existing.getRiderUserId(), count);
            }
        }
        return orderRows > 0 && riderRows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmReceive(String orderNo) {
        OrderInfo order = orderInfoMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderNo(orderNo);
        orderInfo.setOrderStatus(OrderStatusEnum.FINISHED.getCode());
        orderInfo.setUpdateTime(now);
        orderInfo.setLifecycleLog(appendLifecycleLog(
                order.getLifecycleLog(),
                "客户确认完成",
                "客户确认收货，订单完成",
                now
        ));
        int orderRows = orderInfoMapper.updateOrder(orderInfo);

        RiderOrder riderOrder = new RiderOrder();
        riderOrder.setOrderNo(orderNo);
        riderOrder.setRiderStatus(OrderStatusEnum.FINISHED.getCode());
        riderOrder.setFinishTime(now);
        riderOrder.setUpdateTime(now);
        int riderRows = riderOrderMapper.updateRiderOrder(riderOrder);
        if (orderRows > 0 && riderRows > 0) {
            pushOrderFinishedNotice(order.getUserId(), order.getOrderNo());
        }
        return orderRows > 0 && riderRows > 0;
    }

    private void prepareNewOrder(OrderInfo orderInfo) {
        LocalDateTime now = LocalDateTime.now();
        orderInfo.setOrderNo(OrderNoGenerator.generate());
        orderInfo.setGoodsBudget(orderInfo.getGoodsBudget() == null ? BigDecimal.ZERO : orderInfo.getGoodsBudget());
        orderInfo.setRewardPrice(orderInfo.getRewardPrice() == null ? BigDecimal.ZERO : orderInfo.getRewardPrice());
        orderInfo.setOrderPrice(orderInfo.getOrderPrice() == null ? BigDecimal.ZERO : orderInfo.getOrderPrice());
        orderInfo.setIsDeleted(0);
        orderInfo.setOrderStatus(OrderStatusEnum.WAIT_ACCEPT.getCode());
        orderInfo.setPayStatus(orderInfo.getPayStatus() == null ? 0 : orderInfo.getPayStatus());
        orderInfo.setCancelApplyCount(0);
        orderInfo.setCreateTime(now);
        orderInfo.setUpdateTime(now);
        orderInfo.setLifecycleLog(appendLifecycleLog(null, "客户发布订单", "客户发布订单，等待骑手接单", now));
    }

    private void saveOrderImages(Long orderId, List<String> imageUrls) {
        if (orderId == null || imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        List<OrderImage> imageList = new ArrayList<>();
        for (String url : imageUrls) {
            OrderImage image = new OrderImage();
            image.setOrderId(orderId);
            image.setImageUrl(url);
            imageList.add(image);
        }
        orderImageMapper.batchInsertImage(imageList);
    }

    private void fillOrderImages(List<OrderInfoVO> orderList) {
        if (orderList == null || orderList.isEmpty()) {
            return;
        }
        List<Long> orderIds = orderList.stream().map(OrderInfoVO::getId).collect(Collectors.toList());
        List<OrderImage> allImages = orderImageMapper.selectByOrderIdList(orderIds);
        Map<Long, List<String>> imageMap = allImages.stream()
                .collect(Collectors.groupingBy(
                        OrderImage::getOrderId,
                        Collectors.mapping(OrderImage::getImageUrl, Collectors.toList())
                ));
        for (OrderInfoVO vo : orderList) {
            vo.setOrderImageList(imageMap.getOrDefault(vo.getId(), new ArrayList<>()));
        }
    }

    private void normalizeHallParentType(OrderQueryDTO query) {
        if (query == null || query.getParentType() == null) {
            return;
        }
        String parentType = query.getParentType().trim();
        if (parentType.contains("互助")) {
            query.setParentType("跑腿代办");
        }
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void pushOrderFinishedNotice(Long creatorUserId, String orderNo) {
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
            msg.setConversationTitle("订单已完成");
            msg.setConversationAvatar("/static/default-avatar.png");
            msg.setSenderId("system");
            msg.setSenderName("系统通知");
            msg.setSenderAvatar("/static/default-avatar.png");
            msg.setContent("您的订单已确认收货，订单已完成，感谢您的使用");
            msg.setMessageType("order_status");
            msg.setIsDeleted(0);
            msg.setCreateTime(LocalDateTime.now());
            chatWebSocketHandler.pushSystemNotice(msg);
        } catch (Exception e) {
            log.error("推送订单完成通知失败, userId={}, orderNo={}, err={}", creatorUserId, orderNo, e.getMessage());
        }
    }
}