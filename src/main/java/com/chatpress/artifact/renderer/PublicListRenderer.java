package com.chatpress.artifact.renderer;

import com.chatpress.artifact.Artifact;
import com.chatpress.common.HtmlUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
public class PublicListRenderer {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String render(Page<Artifact> page) {
        String articles = page.getContent().stream()
                .map(this::renderCard)
                .collect(Collectors.joining());

        if (articles.isBlank()) {
            articles = "<p class=\"empty\">No articles published yet.</p>";
        }

        String pager = "";
        if (page.getTotalPages() > 1) {
            String prev = page.hasPrevious()
                    ? "<a href=\"/p?page=%d\">← Newer</a>".formatted(page.getNumber() - 1)
                    : "<span class=\"muted\">← Newer</span>";
            String next = page.hasNext()
                    ? "<a href=\"/p?page=%d\">Older →</a>".formatted(page.getNumber() + 1)
                    : "<span class=\"muted\">Older →</span>";
            pager = "<nav class=\"pager\">%s %s</nav>".formatted(prev, next);
        }

        return """
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>ChatPress</title>
                    <style>
                        body {
                            margin: 0; padding: 56px 20px 72px;
                            background: #f7f7f4; color: #242424;
                            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Helvetica Neue", sans-serif;
                            font-size: 17px; line-height: 1.75;
                        }
                        main { max-width: 780px; margin: 0 auto; }
                        h1 {
                            margin: 0 0 8px; font-size: 2rem; font-weight: 650; letter-spacing: 0;
                        }
                        .subtitle { color: #737373; margin: 0 0 32px; font-size: 0.95rem; }
                        .card {
                            padding: 24px 0; border-bottom: 1px solid #dedbd2;
                        }
                        .card h2 {
                            margin: 0 0 6px; font-size: 1.2rem; font-weight: 650;
                        }
                        .card h2 a { color: #171717; text-decoration: none; }
                        .card h2 a:hover { color: #0f766e; }
                        .card .meta { color: #737373; font-size: 0.85rem; margin: 0; }
                        .card .tags { margin-top: 6px; }
                        .tag {
                            display: inline-block; padding: 2px 8px; border-radius: 999px;
                            background: #e0f2f1; color: #0f766e; font-size: 0.75rem;
                            margin-right: 6px;
                        }
                        .pager {
                            display: flex; justify-content: space-between; margin-top: 32px;
                        }
                        .pager a { color: #0f766e; text-decoration-thickness: 1px; }
                        .muted { color: #a3a3a3; }
                        .empty { color: #737373; text-align: center; padding: 48px 0; }
                        @media (max-width: 640px) {
                            body { padding: 32px 16px 48px; font-size: 16px; }
                            h1 { font-size: 1.6rem; }
                        }
                    </style>
                </head>
                <body>
                    <main>
                        <h1>ChatPress</h1>
                        <p class="subtitle">A lightweight knowledge base</p>
                        %s
                        %s
                    </main>
                </body>
                </html>
                """.formatted(articles, pager);
    }

    private String renderCard(Artifact artifact) {
        String tagsHtml = artifact.getTags().stream()
                .map(tag -> "<span class=\"tag\">%s</span>".formatted(HtmlUtils.escapeHtml(tag.getName())))
                .collect(Collectors.joining());

        return """
                <article class="card">
                    <h2><a href="/p/%s">%s</a></h2>
                    <p class="meta">%s</p>
                    %s
                </article>
                """.formatted(
                HtmlUtils.escapeHtml(artifact.getSlug()),
                HtmlUtils.escapeHtml(artifact.getTitle()),
                artifact.getCreatedAt().format(FORMATTER),
                tagsHtml.isBlank() ? "" : "<div class=\"tags\">%s</div>".formatted(tagsHtml)
        );
    }
}
