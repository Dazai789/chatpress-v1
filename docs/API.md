# chatpress-v1 API

## 1. 概述

本文档记录当前已经实现的 API。

当前 API 支持 Markdown 内容发布：

```text
创建 Artifact
-> 或上传 .md 文件导入 Artifact
-> 保存原始内容
-> 渲染 HTML
-> 查询、更新、删除
-> 修改草稿 / 发布状态
-> 通过 /p/{slug} 访问公开页面
```

## 2. 基础路径

后台 JSON API：

```text
/api/artifacts
```

公开页面：

```text
/p/{slug}
```

## 3. 创建 Artifact

```text
POST /api/artifacts
```

### 请求体

Markdown：

```json
{
  "title": "Spring Boot Notes",
  "sourceContent": "# Spring Boot Notes\n\nController receives HTTP requests."
}
```

### 字段说明

- `title` 必填。
- `sourceContent` 必填。
- `slug` 不由请求传入，后端根据 `title` 自动生成，并在重复时自动追加数字后缀。
- `sourceFormat` 由后端固定为 `markdown`。
- Markdown 支持标题、段落、列表、引用、代码块、链接、表格、任务列表、删除线和自动链接。
- 渲染后的 HTML 会经过白名单过滤，阻止 `script`、事件属性和危险链接进入公开页面。
- 创建后默认状态为 `published`。

### 响应体

```json
{
  "id": 1,
  "title": "Spring Boot Notes",
  "slug": "spring-boot-notes",
  "sourceFormat": "markdown",
  "sourceContent": "# Spring Boot Notes\n\nController receives HTTP requests.",
  "renderedHtml": "<h1>Spring Boot Notes</h1>\n<p>Controller receives HTTP requests.</p>\n",
  "status": "published",
  "createdAt": "2026-05-25T20:00:00",
  "updatedAt": "2026-05-25T20:00:00"
}
```

### 可能错误

参数校验失败：

```text
400 Bad Request
```

```json
{
  "code": "VALIDATION_FAILED",
  "message": "Request validation failed",
  "fields": {
    "title": "must not be blank"
  }
}
```

## 4. 导入 Markdown 文件

```text
POST /api/artifacts/import/markdown
Content-Type: multipart/form-data
```

### 表单字段

| 字段 | 必填 | 说明 |
|---|---:|---|
| `file` | 是 | `.md` 文件，UTF-8 文本，最大 2MB。 |
| `title` | 否 | 页面标题。不传时使用文件名去掉 `.md` 后的结果。 |

### 示例

```text
file: Spring Notes.md
title: Spring Boot Notes
```

### 说明

- 只支持 `.md` 文件。
- 空文件会被拒绝。
- 文件超过 2MB 会被拒绝。
- 导入后复用 Artifact 创建逻辑。
- 后端自动生成 `slug`。
- 后端自动生成 `renderedHtml`。
- 创建后默认状态为 `published`。

### 响应体

返回完整 Artifact，格式与 `POST /api/artifacts` 相同。

### 可能错误

文件不合法：

```text
400 Bad Request
```

```json
{
  "code": "INVALID_MARKDOWN_FILE",
  "message": "Only .md files are supported"
}
```

## 5. 列出 Artifacts

```text
GET /api/artifacts
```

### 查询参数

| 参数 | 必填 | 默认值 | 说明 |
|---|---:|---:|---|
| `page` | 否 | `0` | 页码，从 0 开始。 |
| `size` | 否 | `10` | 每页数量，范围 1 到 100。 |
| `q` | 否 | 无 | 按标题模糊搜索。 |
| `status` | 否 | 无 | 按状态筛选，只支持 `draft` 或 `published`。 |

### 示例

```text
GET /api/artifacts?page=0&size=10&q=spring&status=published
```

### 响应体

```json
{
  "items": [
    {
      "id": 1,
      "title": "Spring Boot Notes",
      "slug": "spring-boot-notes",
      "sourceFormat": "markdown",
      "status": "published",
      "createdAt": "2026-05-25T20:00:00",
      "updatedAt": "2026-05-25T20:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalItems": 1,
  "totalPages": 1
}
```

### 说明

- 按 `createdAt` 倒序返回。
- 支持分页、标题搜索和状态筛选。
- 返回摘要信息。
- 不返回 `sourceContent`。
- 不返回 `renderedHtml`。

### 可能错误

查询参数不合法：

```text
400 Bad Request
```

```json
{
  "code": "INVALID_QUERY_PARAMETER",
  "message": "Status must be draft or published"
}
```

## 6. 获取 Artifact 详情

```text
GET /api/artifacts/{id}
```

### 响应体

```json
{
  "id": 1,
  "title": "Spring Boot Notes",
  "slug": "spring-boot-notes",
  "sourceFormat": "markdown",
  "sourceContent": "# Spring Boot Notes\n\nController receives HTTP requests.",
  "renderedHtml": "<h1>Spring Boot Notes</h1>\n<p>Controller receives HTTP requests.</p>\n",
  "status": "published",
  "createdAt": "2026-05-25T20:00:00",
  "updatedAt": "2026-05-25T20:00:00"
}
```

### 可能错误

```text
404 Not Found
```

```json
{
  "code": "ARTIFACT_NOT_FOUND",
  "message": "Artifact not found: 1"
}
```

## 7. 更新 Artifact

```text
PUT /api/artifacts/{id}
```

### 请求体

```json
{
  "title": "Updated Spring Boot Notes",
  "sourceContent": "# Updated Spring Boot Notes\n\nService handles business logic."
}
```

### 说明

- 更新标题和 sourceContent。
- 更新不会改变 `slug`。
- 如果 `sourceContent` 变化，后端重新生成 `renderedHtml`。
- 更新内容不会自动改变 `status`。

### 响应体

```json
{
  "id": 1,
  "title": "Updated Spring Boot Notes",
  "slug": "spring-boot-notes",
  "sourceFormat": "markdown",
  "sourceContent": "# Updated Spring Boot Notes\n\nService handles business logic.",
  "renderedHtml": "<h1>Updated Spring Boot Notes</h1>\n<p>Service handles business logic.</p>\n",
  "status": "published",
  "createdAt": "2026-05-25T20:00:00",
  "updatedAt": "2026-05-25T20:10:00"
}
```

### 可能错误

- `400 Bad Request`：参数校验失败。
- `404 Not Found`：artifact 不存在。

## 8. 修改发布状态

```text
PUT /api/artifacts/{id}/status
```

### 请求体

改成草稿：

```json
{
  "status": "draft"
}
```

改成发布：

```json
{
  "status": "published"
}
```

### 响应体

返回完整 Artifact：

```json
{
  "id": 1,
  "title": "Spring Boot Notes",
  "slug": "spring-boot-notes",
  "sourceFormat": "markdown",
  "sourceContent": "# Spring Boot Notes",
  "renderedHtml": "<h1>Spring Boot Notes</h1>\n",
  "status": "draft",
  "createdAt": "2026-05-25T20:00:00",
  "updatedAt": "2026-05-25T20:15:00"
}
```

### 说明

- 只能传 `draft` 或 `published`。
- 草稿仍可通过后台详情接口查看。
- 草稿不能通过公开页面访问。

## 9. 删除 Artifact

```text
DELETE /api/artifacts/{id}
```

### 响应

```text
204 No Content
```

### 可能错误

```text
404 Not Found
```

```json
{
  "code": "ARTIFACT_NOT_FOUND",
  "message": "Artifact not found: 1"
}
```

## 10. 公开页面

```text
GET /p/{slug}
```

### 说明

- 返回完整 HTML 页面。
- 只展示 `published` 状态的 Artifact。
- `draft` 状态返回 404。
- 不存在的 slug 返回 404。
- 当前页面样式写在 `PublicPageRenderer` 的 HTML 字符串中。

### 示例

```text
GET /p/spring-boot-notes
```

响应类型：

```text
text/html
```

## 11. 当前错误响应格式

JSON API 错误统一返回：

```json
{
  "code": "ERROR_CODE",
  "message": "Human readable message"
}
```

参数校验失败时会额外返回字段错误：

```json
{
  "code": "VALIDATION_FAILED",
  "message": "Request validation failed",
  "fields": {
    "title": "must not be blank",
    "sourceContent": "must not be blank"
  }
}
```

当前错误码：

| code | HTTP 状态 | 场景 |
|---|---:|---|
| `VALIDATION_FAILED` | 400 | 请求参数不合法。 |
| `INVALID_REQUEST_BODY` | 400 | 请求体缺失或 JSON 格式错误。 |
| `INVALID_PATH_VARIABLE` | 400 | 路径参数类型不合法。 |
| `INVALID_QUERY_PARAMETER` | 400 | 查询参数不合法。 |
| `INVALID_MARKDOWN_FILE` | 400 | Markdown 导入文件不合法。 |
| `ARTIFACT_NOT_FOUND` | 404 | artifact 不存在。 |
| `METHOD_NOT_ALLOWED` | 405 | HTTP 方法不支持。 |
| `UNSUPPORTED_MEDIA_TYPE` | 415 | Content-Type 不支持。 |

公开页面接口是 HTML 页面入口，404 时不返回 JSON 错误体。

## 12. 暂不实现的 API

当前不做：

- 登录 / 注册。
- 用户权限。
- 标签。
- 自动读取 AI 平台聊天记录。
- LLM 总结。
- DOCX / PDF 导入。
- MCP endpoint。
- 静态站点导出。
