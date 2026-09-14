package com.wwl.mapper;

import com.wwl.model.entity.OrderImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 订单图片 Mapper
 */
@Mapper
public interface OrderImageMapper {

    /**
     * 新增单张图片
     * @param orderImage 图片实体
     * @return 受影响行数
     */
    int insertImage(OrderImage orderImage);

    /**
     * 批量新增图片
     * @param list 图片集合
     * @return 受影响行数
     */
    int batchInsertImage(@Param("list") List<OrderImage> list);

    /**
     * 根据订单ID查询该订单所有图片
     * @param orderId 订单ID
     * @return 图片列表
     */
    List<OrderImage> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 批量根据订单ID集合查询图片（分页列表回填图片专用）
     * @param orderIds 订单ID集合
     * @return 图片列表
     */
    List<OrderImage> selectByOrderIdList(@Param("orderIds") List<Long> orderIds);


    /**
     * 根据图片ID删除单张图片
     * @param id 图片ID
     * @return 受影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据订单ID批量删除该订单下所有图片
     * @param orderId 订单ID
     * @return 受影响行数
     */
    int deleteByOrderId(@Param("orderId") Long orderId);

    /**
     * 修改单张图片地址
     * @param id 图片ID
     * @param imageUrl 图片链接
     * @return 受影响行数
     */
    int updateImageUrl(@Param("id") Long id, @Param("imageUrl") String imageUrl);
}
