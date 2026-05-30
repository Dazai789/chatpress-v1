package com.chatpress.artifact.renderer;

import com.chatpress.artifact.Artifact;

import com.chatpress.common.HtmlUtils;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class AdminDetailRenderer {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String render(Artifact artifact) {
        String status = artifact.getStatus().name().toLowerCase(Locale.ROOT);

        return """
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <link rel="icon" href="data:,">
                    <title>%s - Admin</title>
                    <style>
                        body {
                            margin: 0;
                            background: #f5f5f2;
                            color: #242424;
                            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Helvetica Neue", sans-serif;
                            font-size: 15px;
                            line-height: 1.6;
                        }

                        header {
                            border-bottom: 1px solid #dedbd2;
                            background: #ffffff;
                        }

                        .shell {
                            max-width: 980px;
                            margin: 0 auto;
                            padding: 24px;
                        }

                        .topbar {
                            display: flex;
                            align-items: center;
                            justify-content: space-between;
                            gap: 16px;
                        }

                        h1 {
                            margin: 0;
                            font-size: 1.5rem;
                            font-weight: 650;
                            letter-spacing: 0;
                        }

                        h2 {
                            margin: 28px 0 12px;
                            font-size: 1rem;
                            letter-spacing: 0;
                        }

                        a {
                            color: #0f766e;
                            text-decoration-thickness: 1px;
                            text-underline-offset: 3px;
                        }

                        .button-link {
                            display: inline-flex;
                            align-items: center;
                            height: 36px;
                            padding: 0 12px;
                            border-radius: 6px;
                            background: #0f766e;
                            color: #ffffff;
                            text-decoration: none;
                        }

                        .actions {
                            display: flex;
                            align-items: center;
                            gap: 10px;
                        }

                        .secondary-link {
                            display: inline-flex;
                            align-items: center;
                            height: 34px;
                            padding: 0 12px;
                            border: 1px solid #c9c6bd;
                            border-radius: 6px;
                            background: #ffffff;
                            text-decoration: none;
                        }

                        .danger-link {
                            display: inline-flex;
                            align-items: center;
                            height: 34px;
                            padding: 0 12px;
                            border: 1px solid #fecaca;
                            border-radius: 6px;
                            background: #fff7f7;
                            color: #b91c1c;
                            text-decoration: none;
                        }

                        .meta {
                            display: grid;
                            grid-template-columns: 150px 1fr;
                            gap: 8px 16px;
                            margin: 24px 0;
                            padding: 16px;
                            border: 1px solid #dedbd2;
                            background: #ffffff;
                        }

                        .meta dt {
                            color: #525252;
                            font-weight: 650;
                        }

                        .meta dd {
                            margin: 0;
                            min-width: 0;
                        }

                        .status {
                            display: inline-block;
                            min-width: 76px;
                            padding: 3px 8px;
                            border-radius: 999px;
                            background: #e7eee9;
                            color: #166534;
                            font-size: 0.82rem;
                            text-align: center;
                        }

                        .status.draft {
                            background: #eee7db;
                            color: #92400e;
                        }

                        pre,
                        .preview {
                            margin: 0;
                            padding: 16px;
                            border: 1px solid #dedbd2;
                            border-radius: 6px;
                            background: #ffffff;
                            overflow-x: auto;
                        }

                        pre {
                            white-space: pre-wrap;
                            font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace;
                            line-height: 1.6;
                        }

                        .preview h1,
                        .preview h2,
                        .preview h3 {
                            line-height: 1.25;
                            letter-spacing: 0;
                        }

                        .preview h1:first-child,
                        .preview h2:first-child,
                        .preview h3:first-child,
                        .preview p:first-child {
                            margin-top: 0;
                        }

                        .preview p:last-child {
                            margin-bottom: 0;
                        }

                        @media (max-width: 720px) {
                            .shell {
                                padding: 18px;
                            }

                            .topbar {
                                align-items: flex-start;
                                flex-direction: column;
                            }

                            .actions {
                                flex-wrap: wrap;
                            }

                            .meta {
                                grid-template-columns: 1fr;
                            }
                        }
                    </style>
                </head>
                <body>
                    <header>
                        <div class="shell topbar">
                            <h1>%s</h1>
                            <div class="actions">
                                <a class="secondary-link" href="/admin/artifacts/%d/edit">Edit</a>
                                <a class="danger-link" href="/admin/artifacts/%d/delete">Delete</a>
                                <a class="button-link" href="/admin/artifacts">Back to list</a>
                            </div>
                        </div>
                    </header>
                    <main class="shell">
                        <dl class="meta">
                            <dt>Status</dt>
                            <dd><span class="status %s">%s</span></dd>
                            <dt>Slug</dt>
                            <dd><code>%s</code></dd>
                            <dt>Public</dt>
                            <dd>%s</dd>
                            <dt>Created</dt>
                            <dd>%s</dd>
                            <dt>Updated</dt>
                            <dd>%s</dd>
                        </dl>
                        <h2>Markdown</h2>
                        <pre>%s</pre>
                        <h2>Preview</h2>
                        <div class="preview">
                            %s
                        </div>
                    </main>
                </body>
                </html>
                """.formatted(
                HtmlUtils.escapeHtml(artifact.getTitle()),
                HtmlUtils.escapeHtml(artifact.getTitle()),
                artifact.getId(),
                artifact.getId(),
                HtmlUtils.escapeHtml(status),
                HtmlUtils.escapeHtml(status),
                HtmlUtils.escapeHtml(artifact.getSlug()),
                publicLink(artifact),
                artifact.getCreatedAt().format(DATE_TIME_FORMATTER),
                artifact.getUpdatedAt().format(DATE_TIME_FORMATTER),
                HtmlUtils.escapeHtml(artifact.getSourceContent()),
                artifact.getRenderedHtml()
        );
    }

    private String publicLink(Artifact artifact) {
        if (artifact.getStatus() != Artifact.Status.PUBLISHED) {
            return "Draft";
        }
        return "<a href=\"/p/%s\">/p/%s</a>".formatted(
                HtmlUtils.escapeHtml(artifact.getSlug()),
                HtmlUtils.escapeHtml(artifact.getSlug())
        );
    }
}
