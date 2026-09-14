# 智慧校园墙（Smart Campus Wall）

毕业设计项目，一个基于移动端 + Web 的校园文化墙 / 校园论坛综合应用，支持用户发帖互动、校园活动信息聚合与媒体上传浏览。

## 项目结构

前后端分离，仓库由两部分组成：

```
wallProject/
├─ graduation_project    # 后端（Java Spring Boot）
│   ├─ src/               # 源码（三层架构：Controller / Service / Mapper）
│   ├─ sql/               # 建表相关脚本
│   ├─ pom.xml            # Maven 依赖与构建
│   └─ *.sql              # 功能演进追加的 DDL（加字段、软删除等）
├─ uni/                   # 前端（uni-app 客户端）
│   └─ uni-app/           # uni-app 工程，可编译 H5 / App / 小程序
└─ graduation_project.sql # 数据库初始化脚本
```

## 技术栈

- **后端**：Java · Spring Boot · MyBatis · MySQL · 阿里云 OSS（分片上传）
- **前端**：uni-app（Vue 语法），一套代码多端发布
- **架构**：Controller–Service–Mapper 三层架构，含批量实体/模型代码生成与分包重构工具

## 主要功能

- 校园墙发帖/互动：帖子发布、评论（待确认）、点赞
- 内容管理：软删除、订单/取消申请计数、动态扩展字段
- 媒体上传：阿里云 OSS 分片上传接口与对接文档
- 多端覆盖：基于 uni-app 构建的移动端界面

## 快速开始

```bash
# 后端
cd graduation_project
mvn spring-boot:run      # 导入 graduation_project.sql 初始化数据库后

# 前端（uni-app）
cd uni/uni-app
npm install
```

> 数据库相关变更脚本散落在 `graduation_project/` 根部（`add_*.sql`），按需执行。

## 说明

个人毕业设计项目，用于学习与演示；部分辅助脚本（`fix_bom.ps1`、模型生成器、批量改包工具）为开发期效率工具。