# chatpress_V1

chatpress_V1 是一个面向 AI 内容沉淀场景的轻量级知识页面发布系统。项目支持将 AI 对话整理稿或 Markdown 笔记保存为结构化内容，并发布为可访问、可分享的网页。

## 背景

在日常学习、技术调研和项目开发中，大量有价值的信息已经产生于 ChatGPT、Claude、Gemini、Cursor 等 AI 工具的对话过程。相比传统笔记，这类内容通常具有以下特点：

- 生成速度快，但分散在不同聊天窗口或本地文件中。
- 内容有复用价值，但缺少稳定的访问入口。
- 适合整理成知识页面，而不一定适合写成传统博客文章。
- 常以 Markdown 形式保存，具备较低的发布转换成本。

chatpress_V1 的目标是提供一个简洁的发布流程，把这些内容从临时记录转化为可维护的知识页面。

## 核心流程

```text
输入 AI 对话整理稿或 Markdown 笔记
-> 保存原始内容
-> 渲染为 HTML
-> 生成公开访问页面
```

## MVP 范围

当前版本聚焦单一发布链路，计划实现：

- Artifact 创建、编辑、删除、查询
- Markdown 内容保存
- Markdown 到 HTML 的渲染
- 基于 slug 的公开页面访问
- 后台 JSON API

第一版不包含自动 AI 对话导入、账号权限、语义搜索、知识图谱、文件上传、插件系统等扩展能力。这些功能会在基础发布流程稳定后再评估。

## 数据模型

MVP 使用一个核心实体：`Artifact`。

`Artifact` 表示一篇可发布的知识页面，包含标题、slug、原始内容、渲染结果、发布状态和时间信息。

主要字段：

| 字段 | 说明 |
|---|---|
| `id` | 内部主键 |
| `title` | 页面标题 |
| `slug` | 公开 URL 标识 |
| `sourceFormat` | 原始内容格式，MVP 固定为 `markdown` |
| `sourceContent` | 用户输入的原始内容 |
| `renderedHtml` | 渲染后的 HTML |
| `status` | 发布状态 |
| `createdAt` | 创建时间 |
| `updatedAt` | 更新时间 |

## API 设计

MVP API 草案：

```text
POST   /api/artifacts
GET    /api/artifacts
GET    /api/artifacts/{id}
PUT    /api/artifacts/{id}
DELETE /api/artifacts/{id}
GET    /p/{slug}
```

详细设计文档：

- [产品定义](docs/PRODUCT.md)
- [数据模型](docs/DATA_MODEL.md)
- [API 设计](docs/API.md)

## 项目结构

```text
.
├── README.md
├── pom.xml
├── mvnw
├── mvnw.cmd
├── src/
│   ├── main/
│   └── test/
└── docs/
    ├── PRODUCT.md
    ├── DATA_MODEL.md
    └── API.md
```

## 技术栈规划

后端计划采用：

- Java 21
- Spring Boot 3.5.14
- Maven
- H2
- Spring Data JPA
- Markdown 渲染库

技术选型会围绕后端 CRUD、数据持久化和内容渲染这三个核心目标逐步推进。

## 项目状态

当前项目处于早期实现阶段，已完成产品边界、数据模型、API 草案和 Spring Boot 项目骨架。下一阶段将实现 Artifact 的基础 CRUD 流程。
