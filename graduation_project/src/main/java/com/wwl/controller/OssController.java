package com.wwl.controller;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.wwl.common.result.Result;
import com.wwl.common.util.AliyunOSSProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 阿里云OSS STS临时凭证控制器
 * 前端直传OSS获取临时上传密钥
 */
@RestController
@RequestMapping("/oss")
public class OssController {

    @Autowired
    private AliyunOSSProperties aliyunOSSProperties;

    /**
     * 获取OSS STS临时上传令牌
     * 有效期30分钟，前端使用临时密钥上传图片
     */
    @GetMapping("/sts")
    public Result getStsToken() {
        try {
            DefaultProfile profile = DefaultProfile.getProfile(
                    aliyunOSSProperties.getRegion(),
                    aliyunOSSProperties.getAccessKeyId(),
                    aliyunOSSProperties.getAccessKeySecret()
            );
            IAcsClient client = new DefaultAcsClient(profile);

            AssumeRoleRequest request = new AssumeRoleRequest();
            // 配置授权角色
            request.setRoleArn("acs:ram::1136849449429979:role/oss-sts-upload-role");
            request.setRoleSessionName("uniapp-upload");
            // 临时凭证有效期30分钟，降低密钥泄露风险
            request.setDurationSeconds(1800L);

            // 上传权限策略：允许表白墙、订单、组织、头像、失物招领五个目录。
            String policy = "{\n" +
                    "  \"Version\": \"2015-11-01\",\n" +
                    "  \"Statement\": [\n" +
                    "    {\n" +
                    "      \"Effect\": \"Allow\",\n" +
                    "      \"Action\": [\n" +
                    "        \"oss:PutObject\",\n" +
                    "        \"oss:GetObject\",\n" +
                    "        \"oss:AbortMultipartUpload\",\n" +
                    "        \"oss:ListParts\",\n" +
                    "        \"oss:InitiateMultipartUpload\",\n" +
                    "        \"oss:ListObjects\"\n" +
                    "      ],\n" +
                    "      \"Resource\": [\n" +
                    "        \"acs:oss:*:*:" + aliyunOSSProperties.getBucketName() + "/wall/*\",\n" +
                    "        \"acs:oss:*:*:" + aliyunOSSProperties.getBucketName() + "/wall/*/*\",\n" +
                    "        \"acs:oss:*:*:" + aliyunOSSProperties.getBucketName() + "/wall/*/*/*\",\n" +
                    "        \"acs:oss:*:*:" + aliyunOSSProperties.getBucketName() + "/wall/*/*/*/*\",\n" +
                    "        \"acs:oss:*:*:" + aliyunOSSProperties.getBucketName() + "/order/*\",\n" +
                    "        \"acs:oss:*:*:" + aliyunOSSProperties.getBucketName() + "/order/*/*\",\n" +
                    "        \"acs:oss:*:*:" + aliyunOSSProperties.getBucketName() + "/order/*/*/*\",\n" +
                    "        \"acs:oss:*:*:" + aliyunOSSProperties.getBucketName() + "/organization/*\",\n" +
                    "        \"acs:oss:*:*:" + aliyunOSSProperties.getBucketName() + "/organization/*/*\",\n" +
                    "        \"acs:oss:*:*:" + aliyunOSSProperties.getBucketName() + "/organization/*/*/*\",\n" +
                    "        \"acs:oss:*:*:" + aliyunOSSProperties.getBucketName() + "/organization/*/*/*/*\",\n" +
                    "        \"acs:oss:*:*:" + aliyunOSSProperties.getBucketName() + "/avatar/*\",\n" +
                    "        \"acs:oss:*:*:" + aliyunOSSProperties.getBucketName() + "/avatar/*/*\",\n" +
                    "        \"acs:oss:*:*:" + aliyunOSSProperties.getBucketName() + "/avatar/*/*/*\",\n" +
                    "        \"acs:oss:*:*:" + aliyunOSSProperties.getBucketName() + "/lostfound/*\",\n" +
                    "        \"acs:oss:*:*:" + aliyunOSSProperties.getBucketName() + "/lostfound/*/*\",\n" +
                    "        \"acs:oss:*:*:" + aliyunOSSProperties.getBucketName() + "/lostfound/*/*/*\",\n" +
                    "        \"acs:oss:*:*:" + aliyunOSSProperties.getBucketName() + "/lostfound/*/*/*/*\"\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            request.setPolicy(policy);
            AssumeRoleResponse response = client.getAcsResponse(request);
            AssumeRoleResponse.Credentials credentials = response.getCredentials();

            Map<String, Object> map = new HashMap<>();
            map.put("accessKeyId", credentials.getAccessKeyId());
            map.put("accessKeySecret", credentials.getAccessKeySecret());
            map.put("securityToken", credentials.getSecurityToken());
            map.put("expiration", credentials.getExpiration());
            map.put("bucket", aliyunOSSProperties.getBucketName());
            map.put("region", aliyunOSSProperties.getRegion());
            map.put("endpoint", aliyunOSSProperties.getEndpoint());
            return Result.success(map);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("获取STS Token失败: " + e.getMessage());
            return Result.error("获取OSS临时上传凭证失败：" + e.getMessage());
        }
    }

    /**
     * 测试OSS配置是否加载成功
     */
    @GetMapping("/test")
    public Result testConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("endpoint", aliyunOSSProperties.getEndpoint());
        config.put("bucketName", aliyunOSSProperties.getBucketName());
        config.put("region", aliyunOSSProperties.getRegion());
        config.put("accessKeyId", aliyunOSSProperties.getAccessKeyId() != null ? "已配置" : "未配置");
        config.put("accessKeySecret", aliyunOSSProperties.getAccessKeySecret() != null ? "已配置" : "未配置");
        return Result.success(config);
    }
}
