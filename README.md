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
- Markdown 文件导入和渲染，HTML 安全过滤（XSS 防护）。
- slug 自动生成和唯一性处理。
- draft / published 状态控制。
- `/p` 公开知识库首页（文章卡片 + 分页）+ `/p/{slug}` 文章页（代码语法高亮）。
- 后台 Markdown 编辑器实时预览（marked.js）。
- 后台列表、新建、详情、编辑、删除确认、导入页面和操作日志页。
- User 表、BCrypt 密码加密。
- 注册 / 登录 API + JWT 认证（Bearer token + 表单登录并存）。
- 用户数据隔离（按 created_by 过滤，越权返回 404）。
- H2 file 数据库、H2 test 数据库和 Flyway 迁移（V1-V5）。
- MySQL profile 预留 + 复合索引（V4）。
- 统一错误响应（14 种异常类型 + 兜底）。
- AOP 操作日志（创建/编辑/删除/导入自动记录，`/admin/logs` 可查看）。
- 限流保护（登录/注册 + artifact 写 API，按方法隔离计数）。
- CSRF 保护 + Session 过期友好提示。
- 事务管理（编辑表单原子更新）+ 竞态条件兜底（409 DATA_CONFLICT）。
- **标签系统**（多对多，创建时自动复用/新建，按标签筛选）。
- **全文搜索**（关键词同时匹配标题和正文）。
- **公开页缓存**（Redis Cache-Aside，更新/删除时自动失效，测试环境内存缓存）。
- **Swagger API 文档**（`/swagger` UI + `/api/docs` JSON）。
- **异步线程池**（ThreadPoolTaskExecutor，发布后异步统计，异常日志）。
- **Docker**（多阶段构建 + docker-compose MySQL + Redis）。
- **GitHub Actions CI**（push 自动跑 `mvn test`）。
- MySQL profile + Flyway V1-V6（迁移语法已通过 MySQL 兼容验证，docker-compose 一键部署）。
- **MyBatis-Plus**（与 JPA 共存，复杂多条件联表查询手写 SQL）。
- MockMvc 测试（82 个全部通过）。

## 技术栈

- Java 21 / Spring Boot 3.5.14 / Maven
- Spring MVC / Spring Data JPA / MyBatis-Plus / Spring Security / Spring AOP
- H2（默认）/ MySQL profile
- Flyway / Bean Validation
- CommonMark 及扩展（表格、任务列表、删除线、自动链接）
- Jsoup（HTML 安全过滤，协议白名单）
- JWT（jjwt 0.12.6）/ BCrypt
- JUnit 5 / MockMvc

默认本地启动使用 H2 file 数据库，数据保存在 `./data/chatpress`。测试使用 `test` profile 和 H2 内存数据库。

MySQL profile 通过环境变量配置。 `docker-compose.yml` 已内置 MySQL + Redis，一键启动：
```bash
docker compose up -d
```

单独配置 MySQL：
```text
MYSQL_URL=jdbc:mysql://localhost:3306/chatpress
MYSQL_USERNAME=root
MYSQL_PASSWORD=...
```

如需 Redis 缓存，追加：
```text
spring.cache.type=redis
REDIS_HOST=localhost
REDIS_PORT=6379
```

## 核心入口

```text
GET  /p/{slug}
GET  /admin/artifacts
GET  /admin/logs
POST /api/artifacts
POST /api/artifacts/import/markdown
POST /api/auth/login
POST /api/auth/register
```

完整接口见 API 参考。Swagger UI 在 `/swagger`。

## 快速启动

```bash
# 本地开发（H2，无需外部依赖）
mvn spring-boot:run

# Docker 一键启动（MySQL + Redis + App）
docker compose up -d

# 仅跑测试
mvn test
```

## 文档

技术文档统一在 Obsidian 中维护：`项目与工具/7 API 参考`、`项目与工具/2 Artifact 数据模型`、`项目与工具/8 产品定义`。

## 项目结构

```text
src/main/java/com/chatpress/
  artifact/          — 核心域（实体、服务、DTO、渲染器、Controller）
  auth/              — 认证（User、登录/注册、JWT）
  security/          — Spring Security 配置、JWT 过滤器
  common/            — 全局异常处理、AOP 切面、限流器
src/main/resources/
  db/migration/      — Flyway 迁移脚本 (V1-V5)
src/test/java/com/chatpress/
  artifact/          — Artifact 相关测试
  auth/              — 认证测试
  common/            — 限流器测试
```
