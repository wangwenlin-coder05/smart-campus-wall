package com.wwl.task;

import com.wwl.service.UploadTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 上传任务过期清理定时任务
 * 每日凌晨2点自动清理超时未完成分片上传任务
 */
@Component
public class UploadTaskCleanTask {

    @Autowired
    private UploadTaskService uploadTaskService;

    /**
     * 定时清理过期上传任务
     * cron表达式：0 0 2 * * ?  每日凌晨2点整执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredTasks() {
        try {
            int count = uploadTaskService.cleanExpiredTasks();
            if (count > 0) {
                System.out.println("=== 定时清理过期上传任务 ===");
                System.out.println("本次清理任务数量：" + count);
                System.out.println("任务执行时间：" + LocalDateTime.now());
                System.out.println("===========================");
            }
        } catch (Exception e) {
            System.err.println("清理过期上传任务异常：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
