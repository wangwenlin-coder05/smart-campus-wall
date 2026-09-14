package com.wwl.mapper;

import com.wwl.model.dto.OrderInfoDTO;
import com.wwl.model.dto.OrderQueryDTO;
import com.wwl.model.entity.OrderInfo;
import com.wwl.model.vo.OrderHallVO;
import com.wwl.model.vo.OrderInfoVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单数据访问层
 */
@Mapper
public interface OrderInfoMapper {

    /**
     * 新增跑腿订单
     * @param orderInfo 订单实体
     * @return 受影响行数
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO order_info(order_no,user_id,rider_id,parent_type,sub_type,start_address_id,end_address_id,fetch_code,remark,reward_price,goods_budget,expect_time,order_price,pay_status,order_status,cancel_apply_count,lifecycle_log) " +
            "VALUES(#{orderNo},#{userId},#{riderId},#{parentType},#{subType},#{startAddressId},#{endAddressId},#{fetchCode},#{remark},#{rewardPrice},#{goodsBudget},#{expectTime},#{orderPrice},#{payStatus},#{orderStatus},#{cancelApplyCount},#{lifecycleLog})")
    int insertOrder(OrderInfo orderInfo);

    /**
     * 根据订单ID查询详情（关联查询地址信息）
     * @param id 订单ID
     * @return 订单详情VO
     */
    OrderInfoVO getOrderByIdWithAddress(Long id);

    /**
     * 分页查询接单大厅订单列表
     * @param query 分页+筛选条件
     * @return 接单大厅订单集合
     */
    List<OrderHallVO> selectHallOrderList(@Param("query") OrderQueryDTO query);

    /**
     * 根据用户ID查询个人所有订单（关联查询地址信息）
     * @param userId 用户ID
     * @return 订单集合
     */
    List<OrderInfoVO> getOrderByUserIdWithAddress(Long userId);

    /**
     * 多条件动态查询订单（关联查询地址信息）
     * @param orderInfoDTO 查询条件
     * @return 订单集合
     */
    List<OrderInfoVO> getOrderByConditionWithAddress(OrderInfoDTO orderInfoDTO);

    /**
     * 修改订单信息、状态（接单/完结等场景通用）
     * @param orderInfo 待更新订单数据
     * @return 受影响行数
     */
    int updateOrder(OrderInfo orderInfo);

    /**
     *根据订单号查询订单全部信息
     */
    OrderInfo selectByOrderNo(String orderNo);

    /**
     * 根据ID删除订单
     * @param id 订单ID
     * @return 受影响行数
     */
    int deleteOrder(Long id);
}
