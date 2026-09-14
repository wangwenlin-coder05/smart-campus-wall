package com.wwl.service;

import com.github.pagehelper.PageInfo;
import com.wwl.model.dto.CancelOrderDTO;
import com.wwl.model.dto.OrderInfoDTO;
import com.wwl.model.dto.OrderQueryDTO;
import com.wwl.model.entity.OrderInfo;
import com.wwl.model.vo.OrderHallVO;
import com.wwl.model.vo.OrderInfoVO;

import java.util.List;

/**
 * 订单业务层接口
 */
public interface OrderInfoService {

    /**
     * 新增订单
     */
    //数据包含图片链接
    boolean addOrderData(OrderInfoDTO orderInfo);


    // 新增：新增订单 + 附带图片url列表
    boolean addOrder(OrderInfo orderInfo, List<String> imgUrlList);
    /**
     * 根据id获取订单详情（联表查询完整地址）
     */
    OrderInfoVO getOrderById(Long id);

    /**
     * 获取骑手接单大厅分页列表
     */
    PageInfo<OrderHallVO> getHallOrderList(OrderQueryDTO query);

    /**
     * 获取当前用户所有订单列表（联表查询完整地址）
     */
    List<OrderInfoVO> getUserOrderList(Long userId);

    /**
     * 分页获取当前用户订单列表（联表查询完整地址）
     * @param userId 用户id
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageInfo<OrderInfoVO> getUserOrderPage(Long userId, Long pageNum, Long pageSize);

    /**
     * 订单多条件筛选查询（联表查询完整地址）
     */
    List<OrderInfoVO> orderConditionList(OrderInfoDTO orderInfoDTO);

    /**
     * 修改订单信息、状态
     */
    boolean updateOrder(OrderInfo orderInfo);

    /**
     * 删除订单
     */
    boolean deleteOrder(Long id);

/**
 * 取消订单
 */
    boolean cancelOrder(CancelOrderDTO cancelOrderDTO);

    /**
     * 用户确认收货
     */
    boolean confirmReceive(String orderNo);
}
