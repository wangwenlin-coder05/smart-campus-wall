# OSS 分片上传续传功能使用说明

## 📋 功能概述

本功能实现了阿里云 OSS 的分片上传和断点续传，支持大文件上传、网络中断后继续上传等场景。

### 核心特性
- ✅ 分片上传：将大文件分成多个小片并行上传
- ✅ 断点续传：上传中断后可从上次进度继续
- ✅ 进度保存：实时记录上传进度到数据库
- ✅ 任务管理：查询未完成任务、取消上传等
- ✅ 自动清理：定时清理过期的未完成的任务

---

## 🗄️ 数据库表结构

表名：`upload_task`

执行 SQL 文件创建表：
```bash
mysql -u root -p graduation_project < sql/upload_task.sql
```

---

## 🔌 后端接口说明

### 1. 初始化分片上传

**接口地址**：`POST /upload/init`

**请求参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileName | String | 是 | 文件原始名称 |
| fileSize | Long | 是 | 文件大小（字节） |
| contentType | String | 是 | 文件MIME类型 |
| userUid | String | 是 | 用户UID |

**响应示例**：
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

### 2. 保存上传进度

**接口地址**：`PUT /upload/progress`

**请求参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| uploadId | String | 是 | OSS上传ID |
| checkpoint | String | 是 | 检查点数据（JSON字符串） |
| uploadedParts | Integer | 是 | 已上传分片数 |

**响应示例**：
```json
{
  "code": 200,
  "message": "进度保存成功",
  "data": null
}
```

---

### 3. 获取未完成的上传任务

**接口地址**：`GET /upload/unfinished?userUid=xxx`

**请求参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userUid | String | 是 | 用户UID |

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "uploadId": "D568A473B1E64F7EB9C1234567890ABC",
      "fileName": "video.mp4",
      "fileSize": 104857600,
      "contentType": "video/mp4",
      "objectKey": "uploads/2026/01/15/abc123def456.mp4",
      "userUid": "user_001",
      "status": 1,
      "checkpoint": "{\"uploadId\":\"...\",\"parts\":[...]}",
      "uploadedParts": 10,
      "totalParts": 20,
      "createTime": "2026-01-15T10:30:00",
      "expireTime": "2026-01-16T10:30:00"
    }
  ]
}
```

---

### 4. 获取任务详情

**接口地址**：`GET /upload/task/{uploadId}`

**响应示例**：同上（单个任务对象）

---

### 5. 完成上传

**接口地址**：`POST /upload/complete?uploadId=xxx`

**说明**：所有分片上传完成后调用，标记任务为已完成

---

### 6. 取消上传

**接口地址**：`POST /upload/cancel?uploadId=xxx`

**说明**：取消上传并清理OSS上已上传的分片

---

### 7. 标记上传失败

**接口地址**：`POST /upload/fail?uploadId=xxx`

---

## 💻 前端使用示例

### 安装阿里云 OSS SDK

```bash
npm install ali-oss
```

### 完整上传流程代码

```javascript
import OSS from 'ali-oss';

// ==================== 1. 初始化上传 ====================
async function initUpload(file, userUid) {
  const response = await fetch('/upload/init', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      fileName: file.name,
      fileSize: file.size,
      contentType: file.type,
      userUid: userUid
    })
  });
  
  const result = await response.json();
  if (result.code === 200) {
    return result.data; // { uploadId, objectKey, totalParts, partSize }
  }
  throw new Error(result.message);
}

// ==================== 2. 保存进度 ====================
async function saveProgress(uploadId, checkpoint, uploadedParts) {
  const params = new URLSearchParams({
    uploadId,
    checkpoint: JSON.stringify(checkpoint),
    uploadedParts
  });
  
  await fetch(`/upload/progress?${params}`, {
    method: 'PUT'
  });
}

// ==================== 3. 获取未完成的任务 ====================
async function getUnfinishedTasks(userUid) {
  const response = await fetch(`/upload/unfinished?userUid=${userUid}`);
  const result = await response.json();
  if (result.code === 200) {
    return result.data; // 任务列表
  }
  return [];
}

// ==================== 4. 执行分片上传（支持续传）====================
async function multipartUpload(file, userUid) {
  try {
    // 4.1 检查是否有未完成的上传任务
    const unfinishedTasks = await getUnfinishedTasks(userUid);
    let uploadInfo = null;
    let checkpoint = null;
    
    // 找到相同文件的未完成任务
    const matchedTask = unfinishedTasks.find(
      task => task.fileName === file.name && task.fileSize === file.size
    );
    
    if (matchedTask) {
      console.log('发现未完成任务，准备续传...', matchedTask.uploadId);
      uploadInfo = {
        uploadId: matchedTask.uploadId,
        objectKey: matchedTask.objectKey
      };
      // 解析checkpoint
      if (matchedTask.checkpoint) {
        checkpoint = JSON.parse(matchedTask.checkpoint);
      }
    } else {
      // 4.2 初始化新的上传任务
      console.log('初始化新的上传任务...');
      uploadInfo = await initUpload(file, userUid);
    }
    
    // 4.3 创建OSS客户端（建议使用STS临时凭证）
    const client = new OSS({
      region: 'oss-cn-beijing',
      accessKeyId: 'your-access-key-id', // 实际项目中使用STS
      accessKeySecret: 'your-access-key-secret',
      bucket: 'schoolwall-wwl'
    });
    
    // 4.4 执行分片上传
    const result = await client.multipartUpload(
      uploadInfo.objectKey,
      file,
      {
        partSize: uploadInfo.partSize, // 分片大小
        checkpoint: checkpoint, // 续传的检查点
        progress: async (p, checkpoint) => {
          // 上传进度回调（p为0-1之间的进度值）
          console.log('上传进度:', (p * 100).toFixed(2) + '%');
          
          // 定期保存进度到后端（每10%保存一次）
          if (Math.floor(p * 10) > Math.floor((p - 0.1) * 10)) {
            const uploadedParts = Object.keys(checkpoint.doneParts || {}).length;
            await saveProgress(uploadInfo.uploadId, checkpoint, uploadedParts);
          }
        }
      }
    );
    
    // 4.5 上传完成，标记任务完成
    await fetch(`/upload/complete?uploadId=${uploadInfo.uploadId}`, {
      method: 'POST'
    });
    
    console.log('上传成功！文件URL:', result.url);
    return result.url;
    
  } catch (error) {
    console.error('上传失败:', error);
    
    // 标记上传失败
    if (error.uploadId) {
      await fetch(`/upload/fail?uploadId=${error.uploadId}`, {
        method: 'POST'
      });
    }
    
    throw error;
  }
}

// ==================== 5. 取消上传 ====================
async function cancelUpload(uploadId) {
  await fetch(`/upload/cancel?uploadId=${uploadId}`, {
    method: 'POST'
  });
}

// ==================== 使用示例 ====================
// HTML中的文件选择器
// <input type="file" id="fileInput" />

document.getElementById('fileInput').addEventListener('change', async (e) => {
  const file = e.target.files[0];
  if (!file) return;
  
  const userUid = 'current_user_uid'; // 从登录信息中获取
  
  try {
    const url = await multipartUpload(file, userUid);
    alert('上传成功！文件URL: ' + url);
  } catch (error) {
    alert('上传失败: ' + error.message);
  }
});
```

---

## 🔧 注意事项

### 1. OSS STS 临时凭证（推荐）

生产环境建议使用 STS 临时凭证，避免暴露 AccessKey：

```javascript
// 后端提供STS接口
const stsResponse = await fetch('/oss/sts');
const stsData = await stsResponse.json();

const client = new OSS({
  region: 'oss-cn-beijing',
  accessKeyId: stsData.data.accessKeyId,
  accessKeySecret: stsData.data.accessKeySecret,
  stsToken: stsData.data.securityToken,
  bucket: 'schoolwall-wwl'
});
```

### 2. 分片大小建议

- 小文件（< 100MB）：5MB/片
- 中等文件（100MB - 1GB）：10MB/片
- 大文件（> 1GB）：20-50MB/片

### 3. 过期任务清理

建议添加定时任务清理过期数据：

```java
@Component
public class UploadTaskCleanTask {
    
    @Autowired
    private UploadTaskService uploadTaskService;
    
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanExpiredTasks() {
        int count = uploadTaskService.cleanExpiredTasks();
        System.out.println("清理过期上传任务：" + count + "条");
    }
}
```

### 4. 并发控制

- 同一用户可同时上传多个文件
- 每个文件独立维护 uploadId 和 checkpoint
- 建议在数据库中设置唯一索引防止重复

---

## 📊 状态码说明

| 状态码 | 说明 |
|--------|------|
| 0 | 初始化 - 任务已创建，尚未开始上传 |
| 1 | 上传中 - 正在上传分片 |
| 2 | 已完成 - 所有分片上传完成 |
| 3 | 已取消 - 用户主动取消 |
| 4 | 已失败 - 上传过程中出现错误 |

---

## 🚀 性能优化建议

1. **并行上传**：OSS SDK 默认会并行上传多个分片，可通过 `parallel` 参数控制
2. **断点续传**：合理利用 checkpoint 避免重复上传
3. **CDN加速**：开启 OSS CDN 加速文件访问
4. **压缩传输**：对图片、视频等进行压缩后再上传
5. **秒传功能**：通过文件 MD5 判断是否已存在相同文件

---

## 📞 常见问题

### Q1: checkpoint 数据结构是什么？
A: checkpoint 是 OSS SDK 内部维护的 JSON 对象，包含：
```json
{
  "uploadId": "xxx",
  "doneParts": {
    "1": {"etag": "...", "partNumber": 1},
    "2": {"etag": "...", "partNumber": 2}
  }
}
```

### Q2: 如何处理网络中断？
A: 重新调用 `multipartUpload`，传入之前的 checkpoint 即可自动续传。

### Q3: 过期时间如何设置？
A: 默认为 24 小时，可在 `UploadTaskServiceImpl.initMultipartUpload` 中修改。

---

## 📝 总结

本功能提供了完整的 OSS 分片上传解决方案，包括：
- ✅ 后端接口完整实现
- ✅ 数据库表结构设计
- ✅ 前端使用示例代码
- ✅ 断点续传支持
- ✅ 任务管理和清理

如有问题，请联系开发团队。
