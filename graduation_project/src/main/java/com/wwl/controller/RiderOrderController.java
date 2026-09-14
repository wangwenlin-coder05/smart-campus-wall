package com.wwl.controller;

import com.github.pagehelper.PageInfo;
import com.wwl.common.result.Result;
import com.wwl.model.dto.OrderAcceptDTO;
import com.wwl.model.dto.OrderDisputeDTO;
import com.wwl.model.dto.RiderOrderQueryDTO;
import com.wwl.model.vo.OrderCountVO;
import com.wwl.model.vo.RiderOwnOrderVO;
import com.wwl.service.RiderOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rider/order")
public class RiderOrderController {

    @Autowired
    private RiderOrderService riderOrderService;

    @PostMapping("/accept")
    public Result acceptOrder(@RequestBody OrderAcceptDTO dto) {
        return riderOrderService.acceptOrder(dto) ? Result.success("接单成功") : Result.error("接单失败，请重试");
    }

    @GetMapping("/list")
    public Result<PageInfo<RiderOwnOrderVO>> getRiderOrderList(@Valid RiderOrderQueryDTO dto) {
        PageInfo<RiderOwnOrderVO> pageData = riderOrderService.riderGetOwnOrderList(dto);
        return Result.success(pageData);
    }

    @PostMapping("/update")
    public Result updateOrderStatus(@RequestBody OrderAcceptDTO dto) {
        return riderOrderService.updateOrderStatus(dto) ? Result.success("订单状态更新成功") : Result.error("订单状态更新失败，请重试");
    }

    @PostMapping("/cancel")
    public Result riderCancelOrder(@RequestBody OrderAcceptDTO dto) {
        return riderOrderService.riderCancelOrder(dto) ? Result.success("取消成功，订单已回到大厅") : Result.error("取消失败，请确认订单未取件并填写原因");
    }

    @PostMapping("/dispute")
    public Result disputeRiderOrder(@RequestBody OrderDisputeDTO dto) {
        return riderOrderService.disputeRiderOrder(dto) ? Result.success("处理成功") : Result.error("处理失败，请填写原因后重试");
    }

    @GetMapping("/order-all-count")
    public Result<OrderCountVO> getOrderAllCount(@RequestParam String riderUserId) {
        OrderCountVO vo = riderOrderService.getAllStatusCount(riderUserId);
        return Result.success("获取各状态订单数量", vo);
    }
}
