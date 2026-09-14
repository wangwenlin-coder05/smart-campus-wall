package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.model.dto.LostFoundDTO;
import com.wwl.model.entity.LostFoundApplication;
import com.wwl.model.entity.LostFoundItem;
import com.wwl.service.LostFoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lost-found")
public class LostFoundController {

    @Autowired
    private LostFoundService lostFoundService;

    @PostMapping("/list")
    public Result list(@RequestBody(required = false) LostFoundDTO dto) {
        return Result.success(lostFoundService.list(dto == null ? new LostFoundDTO() : dto));
    }

    @GetMapping("/get/{id}")
    public Result get(@PathVariable("id") Long id, @RequestParam(required = false) String userUid) {
        LostFoundItem item = lostFoundService.get(id, userUid);
        return item == null ? Result.error("物品不存在") : Result.success(item);
    }

    @PostMapping("/publish")
    public Result publish(@RequestBody LostFoundDTO dto) {
        LostFoundItem item = lostFoundService.publish(dto);
        return item == null ? Result.error("发布失败，请填写完整信息") : Result.success(item);
    }

    @PostMapping("/apply")
    public Result apply(@RequestBody LostFoundDTO dto) {
        LostFoundApplication application = lostFoundService.apply(dto);
        return application == null ? Result.error("申请失败，请检查信息或等待已有申请处理") : Result.success(application);
    }

    @PostMapping("/approve")
    public Result approve(@RequestBody LostFoundDTO dto) {
        return lostFoundService.approve(dto) ? Result.success("已同意") : Result.error("操作失败");
    }

    @PostMapping("/reject")
    public Result reject(@RequestBody LostFoundDTO dto) {
        return lostFoundService.reject(dto) ? Result.success("已拒绝") : Result.error("操作失败");
    }

    @PostMapping("/complete")
    public Result complete(@RequestBody LostFoundDTO dto) {
        return lostFoundService.complete(dto) ? Result.success("状态已更新") : Result.error("操作失败");
    }

    @GetMapping("/stats")
    public Result stats() {
        return Result.success(lostFoundService.stats());
    }

    @PostMapping("/notifications")
    public Result notifications(@RequestBody(required = false) LostFoundDTO dto) {
        return Result.success(lostFoundService.notifications(dto == null ? null : dto.getUserUid()));
    }

    @PostMapping("/delete")
    public Result delete(@RequestBody LostFoundDTO dto) {
        if (dto == null || dto.getItemId() == null) {
            return Result.error("参数错误");
        }
        boolean ok = lostFoundService.delete(dto.getItemId(), dto.getUserUid());
        return ok ? Result.success("已删除") : Result.error("删除失败");
    }
}
