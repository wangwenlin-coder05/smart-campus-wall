package com.wwl.mapper;

import com.wwl.model.dto.RiderOrderQueryDTO;
import com.wwl.model.entity.RiderOrder;
import com.wwl.model.vo.OrderCountVO;
import com.wwl.model.vo.RiderOwnOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RiderOrderMapper {

    /**
     * 新增骑手订单记录
     */
    int insertRiderOrder(RiderOrder riderOrder);


    /**
     * 骑手订单分页查询
     */
// 只传整个DTO，返回List
    List<RiderOwnOrderVO> selectRiderSelfOrderPage(@Param("dto") RiderOrderQueryDTO dto);

    /**
     * 根据ID查询骑手订单
     */
    RiderOrder selectById(@Param("id") Long id);

    /**
     * 根据订单号查询骑手订单
     */
    RiderOrder selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据骑手ID查询个人订单列表
     */
    List<RiderOrder> selectListByRiderId(@Param("riderId") String riderId);

    /**
     * 根据主订单ID查询关联骑手订单
     */
    RiderOrder selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据订单编号更新骑手订单信息
     */
    int updateRiderOrder(RiderOrder riderOrder);

    /**
     * 逻辑删除
     */
    int deleteLogicById(@Param("id") Long id);


    /**
     * 统计骑手所有状态订单数量
     */
    OrderCountVO countAllStatus(@Param("riderUserId") String riderUserId);

    /**
     * 统计骑手纠纷中订单数量
     */
    int countDisputeByRiderUserId(@Param("riderUserId") String riderUserId);
}
