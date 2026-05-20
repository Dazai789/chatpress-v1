# ChatPress 数据模型

## 1. 概述

MVP 只使用一个核心数据对象：`Artifact`。

一个 artifact 表示一篇由用户提供内容生成的已发布知识页面。用户提供的内容可以是一篇普通 Markdown 笔记，也可以是一段 AI 对话整理后的 Markdown。

第一版应该支持这个简单流程：

```text
用户写 Markdown，或粘贴 AI 对话整理后的 Markdown
-> 系统保存原始内容
-> 系统渲染 HTML
-> 系统保存渲染后的 HTML
-> 用户通过 slug 打开公开页面
```

## 2. 主表：`artifact`

第一张数据库表是：

```text
artifact
```

它同时保存原始笔记内容和渲染后的页面内容。

## 3. 字段

| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| `id` | Long / BIGINT | 是 | 主键，唯一内部标识。 |
| `title` | String / VARCHAR | 是 | 用户可读的笔记标题。 |
| `slug` | String / VARCHAR | 是 | URL 友好的公开标识。 |
| `source_format` | String / VARCHAR | 是 | 原始内容格式。MVP 值为 `markdown`。 |
| `source_content` | Text / CLOB | 是 | 用户提供的原始内容，可以是 Markdown 笔记或 AI 对话整理内容。 |
| `rendered_html` | Text / CLOB | 是 | 从 `source_content` 生成的 HTML。 |
| `status` | String / VARCHAR | 是 | 发布状态。MVP 值为 `draft`、`published`。 |
| `created_at` | Timestamp | 是 | artifact 创建时间。 |
| `updated_at` | Timestamp | 是 | artifact 最后更新时间。 |

## 4. 字段说明

### `id`

`id` 用于后端内部识别。

示例：

```text
1
2
3
```

### `title`

标题会展示在笔记列表和公开页面上。

示例：

```text
My Java Learning Notes
```

### `slug`

`slug` 用于公开 URL。

示例：

```text
my-java-learning-notes
```

公开页面示例：

```text
/p/my-java-learning-notes
```

`slug` 应该唯一。

### `source_format`

`source_format` 记录用户提供内容的格式。

MVP 只需要支持这个值：

```text
markdown
```

未来可能支持：

```text
html
txt
docx
pdf
```

不要在 MVP 实现未来格式。

### `source_content`

这个字段保存用户的原始内容。

例如：

```markdown
# My Java Learning Notes

Spring Boot helps us build web applications quickly.
```

系统需要保留原始内容，这样用户之后才能编辑笔记或继续整理 AI 对话内容。

### `rendered_html`

这个字段保存生成后的 HTML。

例如：

```html
<h1>My Java Learning Notes</h1>
<p>Spring Boot helps us build web applications quickly.</p>
```

MVP 阶段可以在每次创建或更新笔记时重新生成这个字段。

### `status`

第一版只需要两个状态：

```text
draft
published
```

如果第一版想再简化，可以先把所有笔记都存为 `published`。

### `created_at`

创建时间。

这个值应该在 artifact 首次创建时设置。

### `updated_at`

最后更新时间。

这个值应该在用户编辑 artifact 时变化。

## 5. 约束

MVP 应该强制这些规则：

- `title` 不能为空。
- `slug` 不能为空。
- `slug` 必须唯一。
- `source_format` 在第一版必须是 `markdown`。
- `source_content` 不能为空。
- 当 `source_content` 变化时，应该重新生成 `rendered_html`。

## 6. 示例记录

```text
id: 1
title: My Java Learning Notes
slug: my-java-learning-notes
source_format: markdown
source_content: # My Java Learning Notes
rendered_html: <h1>My Java Learning Notes</h1>
status: published
created_at: 2026-05-20T19:30:00
updated_at: 2026-05-20T19:30:00
```

## 7. 未来表

MVP 不实现这些表，但未来可能有用：

- `tag`
- `artifact_tag`
- `artifact_version`
- `user`
- `asset`
- `preset`

第一版应该继续聚焦单表 `artifact`。
