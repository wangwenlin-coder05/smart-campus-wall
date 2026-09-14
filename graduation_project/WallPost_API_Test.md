# 表白墙发帖API测试指南

## API信息
- **URL**: `POST /wall/post/add`
- **功能**: 发布新帖子（限制每日最多100条）
- **请求方式**: POST
- **Content-Type**: application/json

## 请求参数说明

根据WallPost实体类，以下是可用的请求字段：

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userUid | String | 是 | 用户UID |
| categoryId | Long | 是 | 一级分类ID |
| subCategoryId | Long | 是 | 二级子分类ID |
| nickname | String | 否 | 昵称 |
| avatar | String | 否 | 头像地址 |
| isAnonymous | Integer | 否 | 是否匿名 (0-实名, 1-匿名) |
| content | String | 是 | 帖子文字内容 |
| mediaUrlList | List<String> | 否 | 图片/视频/音频资源地址列表(最多9个) |
| status | Integer | 否 | 审核状态 (0-待审核, 1-已通过, 2-已拒绝) |
| isTop | Integer | 否 | 是否置顶 (0-否, 1-是) |
| topExpireTime | LocalDateTime | 否 | 置顶过期时间 |
| publishTime | LocalDateTime | 否 | 定时发布时间 |
| isTiming | Integer | 否 | 发布类型 (0-立即发布, 1-定时发布) |

注意：userId、id、viewCount、likeCount、collectCount、commentCount、isDeleted、createTime、updateTime等字段由系统自动处理，无需手动传入。

## 测试用例

### 测试1: 基本发帖（实名）
```json
{
  "userUid": "test_user_001",
  "categoryId": 1,
  "subCategoryId": 1,
  "nickname": "测试用户",
  "avatar": "https://example.com/avatar.jpg",
  "isAnonymous": 0,
  "content": "这是一个测试帖子的内容",
  "status": 1
}
```

### 测试2: 匿名发帖
```json
{
  "userUid": "test_user_001",
  "categoryId": 1,
  "subCategoryId": 1,
  "isAnonymous": 1,
  "content": "这是一个匿名测试帖子的内容",
  "status": 1
}
```

### 测试3: 带媒体资源的帖子
```json
{
  "userUid": "test_user_001",
  "categoryId": 2,
  "subCategoryId": 3,
  "nickname": "测试用户",
  "isAnonymous": 0,
  "content": "这是一个带图片的测试帖子",
  "mediaUrlList": [
    "https://example.com/image1.jpg",
    "https://example.com/image2.jpg"
  ],
  "status": 1
}
```

### 测试4: 定时发布帖子
```json
{
  "userUid": "test_user_001",
  "categoryId": 1,
  "subCategoryId": 1,
  "nickname": "测试用户",
  "isAnonymous": 0,
  "content": "这是一个定时发布的测试帖子",
  "isTiming": 1,
  "publishTime": "2026-05-20T10:00:00",
  "status": 1
}
```

## 预期响应

### 成功响应
```json
{
  "code": 200,
  "message": "发布成功",
  "data": null
}
```

### 失败响应 - 达到发帖上限
```json
{
  "code": 500,
  "message": "今日发帖已达上限，最多100条",
  "data": null
}
```

### 失败响应 - 其他错误
```json
{
  "code": 500,
  "message": "发布失败",
  "data": null
}
```

## 使用curl测试示例

```bash
# 基本发帖测试
curl -X POST http://localhost:8080/wall/post/add \
  -H "Content-Type: application/json" \
  -d '{
    "userUid": "test_user_001",
    "categoryId": 1,
    "subCategoryId": 1,
    "nickname": "测试用户",
    "isAnonymous": 0,
    "content": "这是一个测试帖子的内容",
    "status": 1
  }'
```

## 注意事项

1. 在测试前请确保数据库中有对应的分类ID存在
2. 用户UID必须在系统中存在，否则无法通过发帖限制检查
3. 每个用户每天最多只能发布100条帖子
4. 媒体URL列表最多包含9个元素
5. 如果设置了定时发布(isTiming=1)，则需要提供publishTime字段
6. 帖子默认状态为待审核(0)，如需直接显示需设置为已通过(1)

## 前置条件检查

在进行API测试之前，请确认：
1. 应用服务正在运行
2. 数据库连接正常
3. 存在有效的用户UID
4. 存在有效的分类ID（一级和二级分类）