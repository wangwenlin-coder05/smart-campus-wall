package com.wwl.controller;

import com.github.pagehelper.PageInfo;
import com.wwl.common.result.Result;
import com.wwl.common.util.AliyunOSSOperator;
import com.wwl.mapper.UserAddressMapper;
import com.wwl.mapper.UserMapper;
import com.wwl.model.dto.CancelOrderDTO;
import com.wwl.model.dto.OrderInfoDTO;
import com.wwl.model.dto.OrderQueryDTO;
import com.wwl.model.entity.OrderInfo;
import com.wwl.model.entity.UserAddress;
import com.wwl.model.vo.OrderHallVO;
import com.wwl.service.OrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 订单控制器
 * 跑腿订单相关接口
 */
@Slf4j
//跨域

@RestController
@RequestMapping("/order")
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private AliyunOSSOperator aliyunOSSOperator;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    /**
     * 新增下单订单 + 图片批量上传阿里云，废
     */
    @PostMapping("/add")
    public Result add(OrderInfo orderInfo,
                      @RequestParam(value = "files", required = false) List<MultipartFile> files) {

        // 存放上传成功后的阿里云图片链接
        List<String> imgUrlList = new ArrayList<>();

        try {
            // 遍历所有图片，controller层统一上传到wwl-java-ai存储空间
            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    String url = aliyunOSSOperator.upload(file.getBytes(), file.getOriginalFilename());
                    imgUrlList.add(url);
                }
            }

            // 调用业务层：保存订单 + 批量保存图片链接
            boolean result = orderInfoService.addOrder(orderInfo, imgUrlList);
            return result ? Result.success("订单创建成功") : Result.error("订单创建失败");

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("订单提交或图片上传异常");
        }
    }

    /**
     * 直接提交订单接口
     * 前端逻辑：前端先批量上传图片拿到url，逗号拼接后直接传给后端
     * 后端逻辑：只接收普通字符串参数，不接收文件、不处理上传
     */
    @PostMapping("/addOrderData")
    public Result addOrderData(@RequestBody OrderInfoDTO orderInfo){
        boolean res = orderInfoService.addOrderData(orderInfo);
        return res ? Result.success("订单提交成功") : Result.error("订单提交失败");
    }

    /**
     * 提交订单：前端只传 uid 和 addressNo，后端内部转换为自增ID。
     */
    @PostMapping("/addOrderDataByUid")
    public Result addOrderDataByUid(@RequestBody OrderInfoDTO orderInfo) {
        Long userId = userMapper.selectIdByUid(orderInfo.getUserUid());
        if (userId == null) {
            return Result.error("用户不存在");
        }
        UserAddress start = userAddressMapper.getByAddressNoAndUserId(orderInfo.getStartAddressNo(), userId);
        UserAddress end = userAddressMapper.getByAddressNoAndUserId(orderInfo.getEndAddressNo(), userId);
        if (start == null || end == null) {
            return Result.error("地址不存在");
        }
        orderInfo.setUserId(userId);
        orderInfo.setStartAddressId(start.getId());
        orderInfo.setEndAddressId(end.getId());
        boolean res = orderInfoService.addOrderData(orderInfo);
        return res ? Result.success("订单提交成功") : Result.error("订单提交失败");
    }

    /**
     * 根据订单id查询订单详情
     */
    @GetMapping("/get/{id}")
    public Result getById(@PathVariable("id") Long id){
        return Result.success(orderInfoService.getOrderById(id));
    }

    /**
     * 查询指定用户名下所有订单
     */
    @GetMapping("/user/{userId}")
    public Result userOrder(@PathVariable("userId") Long userId){
        return Result.success(orderInfoService.getUserOrderList(userId));
    }

    /**
     * 分页查询指定用户名下订单
     * @param userId 用户id
     * @param pageNum 页码，默认1
     * @param pageSize 每页条数，默认10
     * @return 分页结果
     */
    @GetMapping("/user/{userId}/page")
    public Result userOrderPage(@PathVariable("userId") Long userId,
                                @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
                                @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize){
        return Result.success(orderInfoService.getUserOrderPage(userId, pageNum, pageSize));
    }

    /**
     * 分页查询当前用户订单：前端只传 uid。
     */
    @GetMapping("/userUid/{uid}/page")
    public Result userOrderPageByUid(@PathVariable("uid") String uid,
                                     @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
                                     @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize) {
        Long userId = userMapper.selectIdByUid(uid);
        if (userId == null) {
            return Result.error("用户不存在");
        }
        return Result.success(orderInfoService.getUserOrderPage(userId, pageNum, pageSize));
    }

    /**
     * 订单多条件筛选查询
     */
    @PostMapping("/search")
    public Result search(@RequestBody OrderInfoDTO orderInfoDTO){
        return Result.success(orderInfoService.orderConditionList(orderInfoDTO));
    }
    /**
     * 骑手接单大厅-查询可接单列表
     */
    @GetMapping("/receiveHallList")
    public Result getReceiveOrderList(OrderQueryDTO query){
        PageInfo<OrderHallVO> pageInfo = orderInfoService.getHallOrderList(query);
        return Result.success(pageInfo);
    }

    /**
     * 用户取消订单
     */
    @PostMapping("/cancelOrder")
    public Result cancelOrder(@RequestBody CancelOrderDTO cancelOrderDTO){
        // 打印参数
        log.info("cancelOrderDTO = {}", cancelOrderDTO);
        try {
            return orderInfoService.cancelOrder(cancelOrderDTO) ? Result.success("取消成功") : Result.error("取消失败");
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户确认收货
     */
    @PostMapping("/confirmReceive")
    public Result confirmReceive(@RequestBody OrderInfoDTO orderInfoDTO){
        return orderInfoService.confirmReceive(orderInfoDTO.getOrderNo()) ? Result.success("确认成功") : Result.error("确认失败");
    }


    /**
     * 修改订单信息、接单、改状态、支付状态更新
     */
    @PutMapping("/update")
    public Result update(@RequestBody OrderInfo orderInfo){
        return orderInfoService.updateOrder(orderInfo) ? Result.success("订单修改成功") : Result.error("修改失败");
    }



    /**
     * 删除订单
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable("id") Long id){
        return orderInfoService.deleteOrder(id) ? Result.success("订单删除成功") : Result.error("删除失败");
    }
}
