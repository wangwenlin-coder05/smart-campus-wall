# 智慧校园墙 · AI 校园服务（Smart Campus Wall）

面向高校的智能化校园文化墙与综合服务平台，将 **AI 大模型对话**、**AI 视觉识别（OCR）** 与校园帖子、失物招领、社团活动等业务场景深度融合。基于前后端分离架构，后端主动能，前端一套 uni-app 多端发布。

## ✨ AI 能力

- **AI 助手 / 智能对话（大模型集成）**
  - 提供 `POST /ai/chat` 接口，支持流式问答，家长/学生/教职工在线咨询校园生活、办事指引等内容。
  - 通过「兼容 OpenAI 协议」的 LLM 网关接入，**模型可配置切换**（如 `deepseek-chat` / `gpt-3.5-turbo`），密钥集中管理于配置/环境变量，不影响业务代码。
  - 后端封装 `AiAssistantService`，前端 uni-app 直接调用，可扩展为校园 AI 客服机器人。

- **AI 视觉识别（百度 OCR）**
  - 集成百度 OCR（`AipOcr`），实现图片文字识别，自动提取票据、照片中的关键信息（如订单票据内容自动解析填入），提升录入效率与准确性。

- **实时消息（WebSocket）**
  - 提供实时聊天与业务计数推送通道（如订单争议/举报计数实时更新），保障即时交互体验。

## 业务功能

- **校园墙帖子**：发帖、评论、点赞、软删除，含订单取消申请计数、动态扩展字段等运营能力
- **失物招领**：寻物/拾遗发布与认领，结合 OCR 快速登记物品信息
- **社团 / 组织活动**：活动发布、报名与组织管理
- **媒体上传**：阿里云 OSS 分片上传，支持大文件、断点续传
- **账号体系**：注册、登录、鉴权

## 技术栈

| 层 | 技术 |
| --- | --- |
| 后端 | Java · Spring Boot · MyBatis · MySQL |
| AI | LLM 大模型（OpenAI 兼容协议，可切模型）· 百度 OCR |
| 实时 | WebSocket |
| 存储 | 阿里云 OSS（分片上传） |
| 架构 | Controller–Service–Mapper 三层架构 |
| 前端 | uni-app（编译 H5 / App / 小程序） |

## 项目结构

```
wallProject/
├─ graduation_project/        # 后端 Spring Boot
│   └─ src/main/java/com/wwl/
│       ├─ controller/        # 接口层（含 AiAssistantController）
│       ├─ service/           # 业务层（含 AiAssistantService）
│       ├─ common/util/       # 工具（BaiDuOcrUtil、TicketPictureParseUtil）
│       └─ websocket/handler/ # 实时聊天/计数推送
├─ uni/                       # 前端 uni-app
└─ graduation_project.sql     # 数据库初始化
```

## 快速开始

```bash
# 后端
cd graduation_project
# 在 application.yml / 环境变量中配置 ai.assistant 与 baidu.ocr 密钥后
mvn spring-boot:run

# 前端（uni-app）
cd uni/uni-app
npm install
```

## 说明

个人毕业设计项目，用于学习与演示。AI 相关密钥请通过配置/环境变量注入，勿提交到仓库。