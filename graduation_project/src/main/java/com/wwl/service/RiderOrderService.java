package com.wwl.service;

import com.github.pagehelper.PageInfo;
import com.wwl.model.dto.OrderAcceptDTO;
import com.wwl.model.dto.OrderDisputeDTO;
import com.wwl.model.dto.RiderOrderQueryDTO;
import com.wwl.model.entity.RiderOrder;
import com.wwl.model.vo.OrderCountVO;
import com.wwl.model.vo.RiderOwnOrderVO;

public interface RiderOrderService {

    /**
     * 新增骑手订单记录
     * @param riderOrder 骑手订单实体
     * @return 影响行数
     */
    int addRiderOrder(RiderOrder riderOrder);

    /**
     * 获取骑手接单大厅分页列表
     * @param dto 查询条件
     * @return 分页结果
     */
    PageInfo<RiderOwnOrderVO> riderGetOwnOrderList(RiderOrderQueryDTO dto);





    /**
     * 骑手接单
     * @param dto 接单参数
     * @return 是否成功
     */
    boolean acceptOrder(OrderAcceptDTO dto);

    /**
     * 更新订单状态
     * @param dto 更新状态参数
     * @return 是否成功
     */
    boolean updateOrderStatus(OrderAcceptDTO dto);

    /**
     * 骑手主动取消订单，订单回到接单大厅
     * @param dto 取消参数
     * @return 是否成功
     */
    boolean riderCancelOrder(OrderAcceptDTO dto);

    /**
     * 骑手处理订单纠纷
     * @param dto 订单处理参数
     * @return 是否成功
     */
    boolean disputeRiderOrder(OrderDisputeDTO dto);

    /**
     * 获取骑手所有状态订单数量
     * @param riderUserId 骑手用户ID
     * @return 订单数量
     */
    OrderCountVO getAllStatusCount(String riderUserId);
}
