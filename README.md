# 🎓 智慧校园墙 · AI 校园服务平台

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-6DB33F?style=for-the-badge&logo=spring)
![MyBatis](https://img.shields.io/badge/MyBatis-3-239639?style=for-the-badge&logo=mybatis)
![uni-app](https://img.shields.io/badge/uni--app-3-000000?style=for-the-badge&logo=uniapp)
![LLM](https://img.shields.io/badge/AI-LLM-C14141?style=for-the-badge&logo=openai)
![WebSocket](https://img.shields.io/badge/WebSocket-实时消息-FF6F00?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-毕业设计-9400D3?style=for-the-badge)

> 面向高校学生与教职工的智能化校园综合服务平台，将 AI 大模型对话、AI 视觉 OCR 与校园帖子、失物招领、社团活动等业务场景深度融合，构建一个有趣、好用、有温度的校园文化墙。

---

## ✨ AI 能力亮点

### 1. 🤖 AI 校园助手（大模型对话）
- **HTTP 流式问答**：前端 uni-app 通过 `POST /ai/chat` 调用，后端封装统一接口。
- **多模型可切换**：兼容 OpenAI 协议，支持 `deepseek-chat` / `gpt-3.5-turbo` / 通义等主流模型，密钥集中管理在 `application.yml` 的 `ai.assistant` 节点。
- **典型问答场景**：校园办事指引、活动安排咨询、失物招领辅助描述。

```
┌──────────────┐     HTTP /ai/chat      ┌───────────────────────┐
│  uni-app     │ ─────────────────────▶ │ AiAssistantController │
│  前端        │ ◀───────────────────── │ AiAssistantService    │
└──────────────┘     回复流式返回        └────────┬──────────────┘
                                                  │
                                                  ▼
                                        ┌──────────────────┐
                                        │  LLM 大模型网关  │
                                        │ (DeepSeek/GPT)   │
                                        └──────────────────┘
```

### 2. 👁️ AI 视觉识别（百度 OCR）
- 集成 `com.baidu.aip.ocr.AipOcr`，自动识别上传图片的文字信息。
- 在**失物招领登记**、**票据图片解析**等场景下，自动提取关键要素（日期、编号、描述），避免用户逐条手动录入。
- 封装为 `BaiDuOcrUtil` 组件，配置 `baidu.ocr.app-id/api-key/secret-key` 即可使用。

### 3. ⚡ WebSocket 实时消息
- `ChatWebSocketHandler`：聊天消息实时推送。
- `DisputeCountWebSocketHandler`：订单争议/举报计数实时更新。

---

## 🧩 业务功能

| 模块 | 能力 |
| --- | --- |
| 校园墙 | 帖子发布、评论、点赞、软删除；订单取消申请计数；动态字段扩展 |
| 失物招领 | 寻物 / 拾遗发布与认领，**OCR 辅助快速登记** |
| 社团活动 | 活动发布、报名、组织管理 |
| 媒体上传 | 阿里云 OSS **分片上传**，支持大文件、断点续传 |
| 账号体系 | 注册 / 登录 / JWT 鉴权 |
| 实时通信 | WebSocket 推送聊天与业务计数 |

---

## 📦 技术架构

```
                        ┌──────────────────────────┐
                        │     uni-app 前端（多端）  │
                        │  H5 / App / 小程序       │
                        └────────────┬─────────────┘
                                     │ HTTP REST / WebSocket
                                     ▼
                ┌────────────────────────────────────┐
                │        Spring Boot 后端             │
                │  ┌───────────┐   ┌──────────────┐  │
                │  │Controller │──▶│   Service    │  │
                │  └───────────┘   └──────┬───────┘  │
                │                         │          │
                │  ┌───────────┐          ▼          │
                │  │WebSocket  │   ┌───────────┐      │
                │  │Handler    │   │   Mapper   │     │
                │  └───────────┘   └─────┬─────┘     │
                └────────────────────────┼────────────┘
                                         ▼
       ┌──────────────────┬──────────────┴──────────────┐
       ▼                  ▼                             ▼
  ┌──────────┐       ┌─────────────┐             ┌──────────────┐
  │  MySQL   │       │   阿里云OSS  │             │  LLM + OCR   │
  │ 数据库   │       │  图片/媒体   │             │  外部AI服务   │
  └──────────┘       └─────────────┘             └──────────────┘
```

**设计**：Controller–Service–Mapper 三层架构，解耦清晰；部分工具组件（模型生成器、批量改包器）为开发期效率工具，已独立于业务代码。

---

## 🚀 快速开始

### 后端
```bash
cd graduation_project

# 1. 初始化数据库
# 执行 graduation_project.sql 建表
# 再按序应用根目录的 add_*.sql 增量 DDL

# 2. 配置 AI 与 OCR 密钥（application.yml 或环境变量）
# ai.assistant.api-key  ai.assistant.base-url  ai.assistant.model
# baidu.ocr.app-id      baidu.ocr.api-key       baidu.ocr.secret-key

# 3. 启动
mvn spring-boot:run
# 默认端口 8080（可在 yml 中调整）
```

### 前端（uni-app）
```bash
cd uni/uni-app
npm install
# 用 HBuilderX / uni-cli 编译到 H5 / App / 小程序
```

---

## 📁 目录结构

```
wallProject/
├─ graduation_project/            # Spring Boot 后端
│   ├─ src/main/java/com/wwl/
│   │   ├─ controller/            # AiAssistantController 等
│   │   ├─ service/               # AiAssistantService / 业务服务
│   │   ├─ common/util/           # BaiDuOcrUtil、TicketPictureParseUtil
│   │   ├─ config/                # AiAssistantProperties 等
│   │   ├─ model/                 # 实体 / DTO
│   │   └─ websocket/handler/     # Chat / DisputeCount
│   ├─ src/main/resources/        # application.yml + MyBatis XML
│   ├─ sql/                       # 建表脚本
│   └─ pom.xml
├─ uni/uni-app/                   # uni-app 前端
└─ graduation_project.sql         # 数据库初始化
```

---

## 📸 项目截图

> 下方为运行效果占位图，部署后替换为实际截图。

![placeholder](https://placehold.co/600x300/6DB33F/ffffff?text=AI+校园助手+Demo)

---

## 📜 说明

- 个人毕业设计项目，用于学习与演示。
- **AI 密钥请通过 `application.yml` 或环境变量注入，勿提交到仓库**。
- 代码风格随学习阶段演进，欢迎讨论与改进。
