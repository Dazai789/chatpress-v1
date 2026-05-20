# chatpress-v1 API

## 1. 概述

本文档定义 chatpress-v1 的 MVP API。

API 需要支持一个简单流程：

```text
创建 Markdown 笔记，或粘贴 AI 对话整理后的 Markdown
-> 保存为 artifact
-> 渲染成 HTML
-> 列出、查看、更新、删除
-> 通过公开 URL 打开
```

MVP API 只需要支持 Markdown artifact。AI 聊天记录导入暂时不做自动化，第一版只要求用户把聊天内容整理或复制成 Markdown 后提交。

## 2. 基础路径

使用两组路由：

```text
/api/artifacts
```

用于 JSON API 操作。

```text
/p/{slug}
```

用于公开页面访问。

## 3. 创建 Artifact

```text
POST /api/artifacts
```

从 Markdown 内容创建一个新的 artifact。这个 Markdown 可以来自普通笔记，也可以来自 AI 聊天记录整理。

### 请求体

```json
{
  "title": "My Java Learning Notes",
  "sourceContent": "# My Java Learning Notes\n\nSpring Boot helps us build web applications quickly."
}
```

### 说明

- `title` 必填。
- `sourceContent` 必填。
- 后端应该生成 `slug`。
- 后端应该把 `sourceFormat` 设置为 `markdown`。
- 后端应该把 `sourceContent` 渲染成 `renderedHtml`。
- 后端应该把初始状态设置为 `published`。

### 响应体

```json
{
  "id": 1,
  "title": "My Java Learning Notes",
  "slug": "my-java-learning-notes",
  "sourceFormat": "markdown",
  "sourceContent": "# My Java Learning Notes\n\nSpring Boot helps us build web applications quickly.",
  "renderedHtml": "<h1>My Java Learning Notes</h1>\n<p>Spring Boot helps us build web applications quickly.</p>",
  "status": "published",
  "createdAt": "2026-05-20T19:30:00",
  "updatedAt": "2026-05-20T19:30:00"
}
```

### 可能错误

```text
400 Bad Request
```

当 `title` 或 `sourceContent` 为空时返回。

## 4. 列出 Artifacts

```text
GET /api/artifacts
```

返回所有 artifacts。

### 响应体

```json
[
  {
    "id": 1,
    "title": "My Java Learning Notes",
    "slug": "my-java-learning-notes",
    "sourceFormat": "markdown",
    "status": "published",
    "createdAt": "2026-05-20T19:30:00",
    "updatedAt": "2026-05-20T19:30:00"
  }
]
```

### 说明

列表响应不需要包含完整的 `sourceContent` 或 `renderedHtml`。

## 5. 获取 Artifact 详情

```text
GET /api/artifacts/{id}
```

根据内部 ID 返回一个 artifact。

### 响应体

```json
{
  "id": 1,
  "title": "My Java Learning Notes",
  "slug": "my-java-learning-notes",
  "sourceFormat": "markdown",
  "sourceContent": "# My Java Learning Notes\n\nSpring Boot helps us build web applications quickly.",
  "renderedHtml": "<h1>My Java Learning Notes</h1>\n<p>Spring Boot helps us build web applications quickly.</p>",
  "status": "published",
  "createdAt": "2026-05-20T19:30:00",
  "updatedAt": "2026-05-20T19:30:00"
}
```

### 可能错误

```text
404 Not Found
```

当 artifact 不存在时返回。

## 6. 更新 Artifact

```text
PUT /api/artifacts/{id}
```

更新一个已有 artifact。

### 请求体

```json
{
  "title": "Updated Java Learning Notes",
  "sourceContent": "# Updated Java Learning Notes\n\nSpring Boot is useful for backend projects."
}
```

### 说明

- `title` 必填。
- `sourceContent` 必填。
- 如果标题变化，后端应该重新生成 `slug`。
- 如果 `sourceContent` 变化，后端应该重新生成 `renderedHtml`。
- 后端应该更新 `updatedAt`。

### 响应体

```json
{
  "id": 1,
  "title": "Updated Java Learning Notes",
  "slug": "updated-java-learning-notes",
  "sourceFormat": "markdown",
  "sourceContent": "# Updated Java Learning Notes\n\nSpring Boot is useful for backend projects.",
  "renderedHtml": "<h1>Updated Java Learning Notes</h1>\n<p>Spring Boot is useful for backend projects.</p>",
  "status": "published",
  "createdAt": "2026-05-20T19:30:00",
  "updatedAt": "2026-05-20T19:45:00"
}
```

### 可能错误

```text
400 Bad Request
```

当 `title` 或 `sourceContent` 为空时返回。

```text
404 Not Found
```

当 artifact 不存在时返回。

## 7. 删除 Artifact

```text
DELETE /api/artifacts/{id}
```

根据内部 ID 删除一个 artifact。

### 响应

```text
204 No Content
```

### 可能错误

```text
404 Not Found
```

当 artifact 不存在时返回。

## 8. 公开页面

```text
GET /p/{slug}
```

返回 artifact 的公开 HTML 页面。

### 示例

```text
GET /p/my-java-learning-notes
```

### 响应

响应应该是一个 HTML 页面。

MVP 阶段页面可以非常简单：

```html
<!doctype html>
<html>
  <head>
    <title>My Java Learning Notes</title>
  </head>
  <body>
    <main>
      <h1>My Java Learning Notes</h1>
      <p>Spring Boot helps us build web applications quickly.</p>
    </main>
  </body>
</html>
```

### 可能错误

```text
404 Not Found
```

当 artifact 不存在时返回。

## 9. 不属于 MVP 的 API 功能

不要在 MVP 实现这些 API 功能：

- 认证。
- 用户级 artifact。
- 私有 artifact。
- 分页。
- 搜索。
- 标签 API。
- 文件上传。
- 自动导入 AI 聊天记录。
- DOCX 上传。
- HTML 上传。
- AI 生成。
- LLM 可读 JSON 或文本端点。
- MCP 端点。
