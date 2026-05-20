# ChatPress

ChatPress 是一个用于整理 AI 对话和 Markdown 笔记，并发布为知识页面的轻量级系统。

它的目标不是做一个完整博客平台，也不是做复杂的 AI 知识库，而是先解决一个很具体的问题：

```text
把有价值的 AI 对话或 Markdown 笔记
整理成可保存、可访问、可分享的网页
```

## 项目定位

很多学习总结、技术记录、项目思路现在都来自 ChatGPT、Claude、Gemini、Cursor 等 AI 工具。但这些内容通常散落在聊天记录、本地文件或临时笔记里，不方便长期保存和分享。

ChatPress 希望提供一个更简单的发布流程：

```text
粘贴 AI 对话整理内容或 Markdown 笔记
-> 保存
-> 渲染成 HTML
-> 生成一个公开页面
```

## MVP 功能

第一版只做最小可用功能：

- 创建笔记
- 编辑笔记
- 删除笔记
- 查看笔记列表
- 查看笔记详情
- 将 Markdown 渲染为 HTML
- 通过 `/p/{slug}` 访问公开页面

## 暂不实现

为了控制项目范围，第一版暂不实现：

- 自动读取 ChatGPT、Claude、Gemini、Cursor 聊天记录
- LLM 集成
- MCP server
- DOCX / PDF 解析
- 语义搜索
- 知识图谱
- 用户账号和权限系统
- Redis
- 插件市场
- 多用户协作

第一版会先支持用户手动粘贴已经整理好的 AI 对话内容或 Markdown 内容。

## 核心数据对象

第一版只有一个核心对象：`Artifact`。

一个 `Artifact` 表示一篇已发布的知识页面，主要字段包括：

- `id`
- `title`
- `slug`
- `sourceFormat`
- `sourceContent`
- `renderedHtml`
- `status`
- `createdAt`
- `updatedAt`

## API 草案

MVP API 计划如下：

```text
POST   /api/artifacts
GET    /api/artifacts
GET    /api/artifacts/{id}
PUT    /api/artifacts/{id}
DELETE /api/artifacts/{id}
GET    /p/{slug}
```

详细说明见：

- [产品定义](./PRODUCT.md)
- [数据模型](./DATA_MODEL.md)
- [API 设计](./API.md)

## 当前状态

项目目前处于规划和设计阶段，已经完成：

- 产品边界定义
- MVP 数据模型设计
- MVP API 草案

下一步会开始创建 Spring Boot 项目骨架，并逐步实现后端 CRUD 流程。

## 技术栈计划

初步计划：

- Java
- Spring Boot
- Maven
- H2 或 MySQL
- Spring Data JPA 或 MyBatis
- Markdown 渲染库

具体技术选型会在实现过程中逐步确定。
