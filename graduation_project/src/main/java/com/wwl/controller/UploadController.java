package com.wwl.controller;

import com.wwl.common.result.Result;
import com.wwl.model.entity.UploadTask;
import com.wwl.service.UploadTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OSS分片上传控制器
 * 提供分片上传初始化、进度保存、断点续传、完成/取消上传等功能
 */
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private UploadTaskService uploadTaskService;

    /**
     * 初始化分片上传
     * 前端开始上传前调用，返回uploadId、文件路径、分片总数、单分片大小等信息
     *
     * @param fileName    文件名称
     * @param fileSize    文件总大小(字节)
     * @param contentType 文件媒体类型
     * @param userUid     上传用户唯一标识
     * @return 初始化上传参数
     */
    @PostMapping("/init")
    public Result initUpload(
            @RequestParam String fileName,
            @RequestParam Long fileSize,
            @RequestParam String contentType,
            @RequestParam String userUid
    ) {
        try {
            return Result.success(uploadTaskService.initMultipartUpload(fileName, fileSize, contentType, userUid));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("初始化上传失败：" + e.getMessage());
        }
    }

    /**
     * 保存分片上传进度
     * 前端上传过程中定时回调，记录断点信息与已传分片
     *
     * @param uploadId      OSS分片上传唯一ID
     * @param checkpoint    前端断点续传校验信息
     * @param uploadedParts 已成功上传分片数量
     * @return 操作提示
     */
    @PutMapping("/progress")
    public Result saveProgress(
            @RequestParam String uploadId,
            @RequestParam String checkpoint,
            @RequestParam Integer uploadedParts
    ) {
        try {
            uploadTaskService.saveCheckpoint(uploadId, checkpoint, uploadedParts);
            return Result.success("进度保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("保存进度失败：" + e.getMessage());
        }
    }

    /**
     * 查询用户未完成上传任务
     * 页面进入上传页自动调用，实现断点续传列表展示
     *
     * @param userUid 用户唯一标识
     * @return 未完成上传任务集合
     */
    @GetMapping("/unfinished")
    public Result getUnfinishedTasks(@RequestParam String userUid) {
        try {
            List<UploadTask> tasks = uploadTaskService.getUnfinishedTasks(userUid);
            return Result.success(tasks);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询任务失败：" + e.getMessage());
        }
    }

    /**
     * 根据上传ID获取上传任务详情
     *
     * @param uploadId OSS上传ID
     * @return 上传任务详情
     */
    @GetMapping("/task/{uploadId}")
    public Result getTaskDetail(@PathVariable String uploadId) {
        try {
            UploadTask task = uploadTaskService.getTaskByUploadId(uploadId);
            if (task != null) {
                return Result.success(task);
            }
            return Result.error("任务不存在");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询任务失败：" + e.getMessage());
        }
    }

    /**
     * 合并所有分片，完成文件上传
     * 前端全部分片上传完毕后调用
     *
     * @param uploadId OSS分片上传ID
     * @return 上传完成提示
     */
    @PostMapping("/complete")
    public Result completeUpload(@RequestParam String uploadId) {
        try {
            uploadTaskService.completeUpload(uploadId);
            return Result.success("上传完成");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("完成上传失败：" + e.getMessage());
        }
    }

    /**
     * 取消分片上传任务
     * 前端手动放弃上传时调用
     *
     * @param uploadId OSS分片上传ID
     * @return 取消结果
     */
    @PostMapping("/cancel")
    public Result cancelUpload(@RequestParam String uploadId) {
        try {
            uploadTaskService.cancelUpload(uploadId);
            return Result.success("已取消上传");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("取消上传失败：" + e.getMessage());
        }
    }

    /**
     * 标记上传任务为上传失败状态
     *
     * @param uploadId OSS分片上传ID
     * @return 操作结果
     */
    @PostMapping("/fail")
    public Result failUpload(@RequestParam String uploadId) {
        try {
            uploadTaskService.failUpload(uploadId);
            return Result.success("已标记失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("操作失败：" + e.getMessage());
        }
    }
}