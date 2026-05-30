package com.chatpress.artifact.renderer;

import com.chatpress.artifact.Artifact;

import com.chatpress.common.HtmlUtils;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AdminDeleteRenderer {

    public String render(Artifact artifact, String csrfToken) {
        String status = artifact.getStatus().name().toLowerCase(Locale.ROOT);

        return """
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <link rel="icon" href="data:,">
                    <title>Delete %s - Admin</title>
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
                            max-width: 760px;
                            margin: 0 auto;
                            padding: 24px;
                        }

                        h1 {
                            margin: 0;
                            font-size: 1.5rem;
                            font-weight: 650;
                            letter-spacing: 0;
                        }

                        a {
                            color: #0f766e;
                            text-decoration-thickness: 1px;
                            text-underline-offset: 3px;
                        }

                        .panel {
                            margin-top: 24px;
                            padding: 18px;
                            border: 1px solid #dedbd2;
                            border-radius: 6px;
                            background: #ffffff;
                        }

                        .meta {
                            display: grid;
                            grid-template-columns: 120px 1fr;
                            gap: 8px 14px;
                            margin: 16px 0 0;
                        }

                        .meta dt {
                            color: #525252;
                            font-weight: 650;
                        }

                        .meta dd {
                            margin: 0;
                        }

                        .actions {
                            display: flex;
                            align-items: center;
                            gap: 12px;
                            margin-top: 22px;
                        }

                        button {
                            height: 40px;
                            padding: 0 16px;
                            border: 1px solid #b91c1c;
                            border-radius: 6px;
                            background: #b91c1c;
                            color: #ffffff;
                            font: inherit;
                            cursor: pointer;
                        }

                        @media (max-width: 720px) {
                            .shell {
                                padding: 18px;
                            }

                            .meta {
                                grid-template-columns: 1fr;
                            }

                            .actions {
                                flex-wrap: wrap;
                            }
                        }
                    </style>
                </head>
                <body>
                    <header>
                        <div class="shell">
                            <h1>Delete Artifact</h1>
                        </div>
                    </header>
                    <main class="shell">
                        <section class="panel">
                            <p>This action will permanently delete this artifact.</p>
                            <dl class="meta">
                                <dt>Title</dt>
                                <dd>%s</dd>
                                <dt>Status</dt>
                                <dd>%s</dd>
                                <dt>Slug</dt>
                                <dd><code>%s</code></dd>
                            </dl>
                            <form method="post" action="/admin/artifacts/%d/delete">
                                <input type="hidden" name="_csrf" value="%s">
                                <div class="actions">
                                    <button type="submit">Delete</button>
                                    <a href="/admin/artifacts/%d">Cancel</a>
                                </div>
                            </form>
                        </section>
                    </main>
                </body>
                </html>
                """.formatted(
                HtmlUtils.escapeHtml(artifact.getTitle()),
                HtmlUtils.escapeHtml(artifact.getTitle()),
                HtmlUtils.escapeHtml(status),
                HtmlUtils.escapeHtml(artifact.getSlug()),
                artifact.getId(),
                HtmlUtils.escapeHtml(csrfToken),
                artifact.getId()
        );
    }
}
