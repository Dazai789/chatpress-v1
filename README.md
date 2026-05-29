# chatpress-v1

chatpress-v1 是一个轻量级 Markdown 页面发布系统。它的核心目标是把 Markdown 内容保存为结构化的 `Artifact`，渲染成安全的 HTML，并通过公开 URL 访问。

## 核心链路

```text
输入标题和 Markdown 内容
-> 保存为 Artifact
-> 自动生成 slug
-> 渲染为 HTML
-> 设置 draft / published 状态
-> 通过 /p/{slug} 公开访问
```

## 已完成能力

- Artifact 创建、列表、详情、编辑、删除。
- Markdown 文件导入和渲染，HTML 安全过滤。
- slug 自动生成和唯一性处理。
- draft / published 状态控制。
- `/p/{slug}` 公开页面。
- 后台列表、新建、详情、编辑、删除确认和导入页面。
- User 表、BCrypt 密码加密。
- 注册 / 登录 API。
- JWT 认证（Bearer token + 表单登录并存）。
- 用户数据隔离（按 created_by 过滤，越权返回 404）。
- H2 file 数据库、H2 test 数据库和 Flyway 迁移。
- MySQL profile 预留。
- 统一错误响应和 MockMvc 测试（64 个全部通过）。

## 欠缺能力

- MySQL 实跑和索引验证。
- Redis、AOP、限流、异步、Docker、CI。

## 技术栈

- Java 21 / Spring Boot 3.5.14 / Maven
- Spring MVC / Spring Data JPA / Spring Security
- H2（默认）/ MySQL profile
- Flyway / Bean Validation
- CommonMark 及扩展（表格、任务列表、删除线、自动链接）
- Jsoup（HTML 安全过滤）
- JWT（jjwt 0.12.6）/ BCrypt
- JUnit 5 / MockMvc

默认本地启动使用 H2 file 数据库，数据保存在 `./data/chatpress`。测试使用 `test` profile 和 H2 内存数据库。

MySQL profile 通过环境变量配置：
```text
MYSQL_URL=jdbc:mysql://localhost:3306/chatpress
MYSQL_USERNAME=root
MYSQL_PASSWORD=...
```

## 核心入口

```text
GET  /p/{slug}
GET  /admin/artifacts
POST /api/artifacts
POST /api/artifacts/import/markdown
```

完整接口见 API 参考。

## 文档

技术文档统一在 Obsidian 中维护：`项目与工具/7 API 参考`、`项目与工具/2 Artifact 数据模型`、`项目与工具/8 产品定义`。

## 项目结构

```text
src/main/java/com/chatpress/v1/
  artifact/
  auth/
  user/
  security/
  common/
  config/
  system/
src/main/resources/
  db/migration/
src/test/java/com/chatpress/v1/
```
