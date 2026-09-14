package com.wwl.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.wwl.model.entity.UploadTask;
import com.wwl.mapper.UploadTaskMapper;
import com.wwl.service.UploadTaskService;
import com.wwl.common.util.AliyunOSSProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * OSS分片上传任务服务实现类
 */
@Service
public class UploadTaskServiceImpl implements UploadTaskService {

    @Autowired
    private UploadTaskMapper uploadTaskMapper;

    @Autowired
    private AliyunOSSProperties aliyunOSSProperties;

    /**
     * 初始化分片上传任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> initMultipartUpload(String fileName, Long fileSize,
                                                   String contentType, String userUid) {
        // 1. 生成OSS存储路径（按日期分类）
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String objectKey = "uploads/" + datePath + "/" + uuid + extension;

        // 2. 创建OSS客户端并初始化分片上传
        OSS ossClient = new OSSClientBuilder().build(
                aliyunOSSProperties.getEndpoint(),
                aliyunOSSProperties.getAccessKeyId(),
                aliyunOSSProperties.getAccessKeySecret()
        );

        try {
            // 3. 初始化分片上传，获取uploadId
            com.aliyun.oss.model.InitiateMultipartUploadRequest initRequest =
                    new com.aliyun.oss.model.InitiateMultipartUploadRequest(
                            aliyunOSSProperties.getBucketName(),
                            objectKey
                    );

            // 设置文件元信息
            com.aliyun.oss.model.ObjectMetadata metadata = new com.aliyun.oss.model.ObjectMetadata();
            metadata.setContentLength(fileSize);
            metadata.setContentType(contentType);
            initRequest.setObjectMetadata(metadata);

            com.aliyun.oss.model.InitiateMultipartUploadResult initResult =
                    ossClient.initiateMultipartUpload(initRequest);
            String uploadId = initResult.getUploadId();

            // 4. 计算总分片数（默认每片5MB）
            long partSize = 5 * 1024 * 1024L;
            int totalParts = (int) Math.ceil((double) fileSize / partSize);

            // 5. 设置过期时间（24小时后）
            LocalDateTime expireTime = LocalDateTime.now().plusHours(24);

            // 6. 保存上传任务到数据库
            UploadTask task = new UploadTask();
            task.setUploadId(uploadId);
            task.setFileName(fileName);
            task.setFileSize(fileSize);
            task.setContentType(contentType);
            task.setObjectKey(objectKey);
            task.setUserUid(userUid);
            task.setStatus(0); // 0-初始化
            task.setUploadedParts(0);
            task.setTotalParts(totalParts);
            task.setExpireTime(expireTime);

            uploadTaskMapper.insertUploadTask(task);

            // 7. 返回初始化信息
            Map<String, Object> result = new HashMap<>();
            result.put("uploadId", uploadId);
            result.put("objectKey", objectKey);
            result.put("totalParts", totalParts);
            result.put("partSize", partSize);

            return result;

        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 保存上传进度（断点续传）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveCheckpoint(String uploadId, String checkpoint, Integer uploadedParts) {
        // 更新数据库中的检查点和已上传分片
        uploadTaskMapper.updateCheckpoint(uploadId, checkpoint, uploadedParts);
        // 更新状态为上传中
        uploadTaskMapper.updateStatus(uploadId, 1);
    }

    /**
     * 获取未完成的上传任务
     */
    @Override
    public List<UploadTask> getUnfinishedTasks(String userUid) {
        // 查询状态：0-初始化 1-上传中 4-已失败 的任务
        List<Integer> statusList = Arrays.asList(0, 1, 4);
        return uploadTaskMapper.selectUnfinishedTasks(userUid, statusList);
    }

    /**
     * 根据uploadId获取任务详情
     */
    @Override
    public UploadTask getTaskByUploadId(String uploadId) {
        return uploadTaskMapper.selectByUploadId(uploadId);
    }

    /**
     * 完成上传任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeUpload(String uploadId) {
        uploadTaskMapper.updateStatus(uploadId, 2); // 2-已完成
    }

    /**
     * 取消上传任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelUpload(String uploadId) {
        // 1. 更新数据库状态
        uploadTaskMapper.updateStatus(uploadId, 3); // 3-已取消

        // 2. 调用OSS API取消分片上传，清理已上传的分片
        OSS ossClient = new OSSClientBuilder().build(
                aliyunOSSProperties.getEndpoint(),
                aliyunOSSProperties.getAccessKeyId(),
                aliyunOSSProperties.getAccessKeySecret()
        );

        try {
            UploadTask task = uploadTaskMapper.selectByUploadId(uploadId);
            if (task != null) {
                ossClient.abortMultipartUpload(
                        new com.aliyun.oss.model.AbortMultipartUploadRequest(
                                aliyunOSSProperties.getBucketName(),
                                task.getObjectKey(),
                                uploadId
                        )
                );
            }
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 标记上传失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void failUpload(String uploadId) {
        uploadTaskMapper.updateStatus(uploadId, 4); // 4-已失败
    }

    /**
     * 清理过期的上传任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanExpiredTasks() {
        return uploadTaskMapper.cleanExpiredTasks();
    }
}
