# OSS 分片上传接口测试用例（Apifox/Postman）

## 基础信息
- **Base URL**: `http://localhost:8080`
- **Content-Type**: `application/json` 或 `application/x-www-form-urlencoded`

---

## 1. 初始化分片上传

**请求方式**: POST  
**请求地址**: `http://localhost:8080/upload/init`  
**Content-Type**: `application/x-www-form-urlencoded`

**请求参数**:
```
fileName=test_video.mp4
fileSize=104857600
contentType=video/mp4
userUid=user_001
```

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "uploadId": "D568A473B1E64F7EB9C1234567890ABC",
    "objectKey": "uploads/2026/01/15/abc123def456.mp4",
    "totalParts": 20,
    "partSize": 5242880
  }
}
```

---

## 2. 保存上传进度

**请求方式**: PUT  
**请求地址**: `http://localhost:8080/upload/progress`  
**Content-Type**: `application/x-www-form-urlencoded`

**请求参数**:
```
uploadId=D568A473B1E64F7EB9C1234567890ABC
checkpoint={"uploadId":"D568A473B1E64F7EB9C1234567890ABC","doneParts":{"1":{"etag":"xxx","partNumber":1}}}
uploadedParts=5
```

**预期响应**:
```json
{
  "code": 200,
  "message": "进度保存成功",
  "data": null
}
```

---

## 3. 获取未完成的上传任务

**请求方式**: GET  
**请求地址**: `http://localhost:8080/upload/unfinished?userUid=user_001`

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "uploadId": "D568A473B1E64F7EB9C1234567890ABC",
      "fileName": "test_video.mp4",
      "fileSize": 104857600,
      "contentType": "video/mp4",
      "objectKey": "uploads/2026/01/15/abc123def456.mp4",
      "userUid": "user_001",
      "status": 1,
      "checkpoint": "{\"uploadId\":\"...\"}",
      "uploadedParts": 5,
      "totalParts": 20,
      "createTime": "2026-01-15T10:30:00",
      "updateTime": "2026-01-15T10:35:00",
      "expireTime": "2026-01-16T10:30:00"
    }
  ]
}
```

---

## 4. 获取任务详情

**请求方式**: GET  
**请求地址**: `http://localhost:8080/upload/task/D568A473B1E64F7EB9C1234567890ABC`

**预期响应**: 同单个任务对象

---

## 5. 完成上传

**请求方式**: POST  
**请求地址**: `http://localhost:8080/upload/complete?uploadId=D568A473B1E64F7EB9C1234567890ABC`

**预期响应**:
```json
{
  "code": 200,
  "message": "上传完成",
  "data": null
}
```

---

## 6. 取消上传

**请求方式**: POST  
**请求地址**: `http://localhost:8080/upload/cancel?uploadId=D568A473B1E64F7EB9C1234567890ABC`

**预期响应**:
```json
{
  "code": 200,
  "message": "已取消上传",
  "data": null
}
```

---

## 7. 标记上传失败

**请求方式**: POST  
**请求地址**: `http://localhost:8080/upload/fail?uploadId=D568A473B1E64F7EB9C1234567890ABC`

**预期响应**:
```json
{
  "code": 200,
  "message": "已标记失败",
  "data": null
}
```

---

## 测试流程

### 完整上传流程测试

1. **步骤1**: 调用【初始化分片上传】接口，获取 uploadId
2. **步骤2**: 前端使用 OSS SDK 进行分片上传，定期调用【保存上传进度】
3. **步骤3**: 上传完成后，调用【完成上传】接口
4. **步骤4**: 调用【获取未完成的上传任务】验证任务状态为已完成

### 断点续传测试

1. **步骤1**: 开始上传文件，保存几次进度
2. **步骤2**: 模拟网络中断（停止上传）
3. **步骤3**: 重新加载页面，调用【获取未完成的上传任务】
4. **步骤4**: 找到未完成的任务，使用 checkpoint 继续上传
5. **步骤5**: 验证可以从上次中断的位置继续

### 取消上传测试

1. **步骤1**: 开始上传文件
2. **步骤2**: 调用【取消上传】接口
3. **步骤3**: 验证数据库中任务状态变为"已取消"
4. **步骤4**: 验证 OSS 上对应的分片已被清理

---

## cURL 示例

### 1. 初始化上传
```bash
curl -X POST "http://localhost:8080/upload/init" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "fileName=test.mp4&fileSize=104857600&contentType=video/mp4&userUid=user_001"
```

### 2. 保存进度
```bash
curl -X PUT "http://localhost:8080/upload/progress" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "uploadId=xxx&checkpoint={}&uploadedParts=5"
```

### 3. 查询未完成任务
```bash
curl -X GET "http://localhost:8080/upload/unfinished?userUid=user_001"
```

### 4. 完成上传
```bash
curl -X POST "http://localhost:8080/upload/complete?uploadId=xxx"
```

---

## 注意事项

1. **userUid**: 实际项目中应从登录 token 中获取，不要前端传递
2. **checkpoint**: 是 OSS SDK 生成的 JSON 对象，需要序列化为字符串
3. **过期时间**: 默认 24 小时，可在代码中修改
4. **并发控制**: 同一用户可以同时上传多个文件
5. **STS凭证**: 生产环境建议使用 STS 临时凭证，避免暴露 AccessKey

---

## 数据库验证 SQL

```sql
-- 查询所有上传任务
SELECT * FROM upload_task ORDER BY create_time DESC;

-- 查询指定用户的未完成任务
SELECT * FROM upload_task 
WHERE user_uid = 'user_001' 
  AND status IN (0, 1, 4) 
  AND is_deleted = 0;

-- 查询过期的任务
SELECT * FROM upload_task 
WHERE expire_time < NOW() 
  AND status IN (0, 1, 4) 
  AND is_deleted = 0;

-- 统计各状态的任务数量
SELECT status, COUNT(*) as count 
FROM upload_task 
WHERE is_deleted = 0 
GROUP BY status;
```
