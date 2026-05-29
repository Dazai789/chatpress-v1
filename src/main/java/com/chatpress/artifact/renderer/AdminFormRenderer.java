package com.chatpress.artifact.renderer;

import com.chatpress.artifact.Artifact;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AdminFormRenderer {

    public String render(String title, String sourceContent, String errorMessage, String csrfToken) {
        return renderNew(title, sourceContent, errorMessage, csrfToken);
    }

    public String renderNew(String title, String sourceContent, String errorMessage, String csrfToken) {
        return renderForm(
                "New Artifact",
                "New Artifact - Admin",
                "/admin/artifacts",
                "/admin/artifacts",
                title,
                sourceContent,
                "published",
                false,
                errorMessage,
                csrfToken
        );
    }

    public String renderEdit(Artifact artifact, String errorMessage, String csrfToken) {
        return renderEdit(
                artifact.getId(),
                artifact.getTitle(),
                artifact.getSourceContent(),
                artifact.getStatus().name().toLowerCase(Locale.ROOT),
                errorMessage,
                csrfToken
        );
    }

    public String renderEdit(Long artifactId, String title, String sourceContent, String status, String errorMessage, String csrfToken) {
        return renderForm(
                "Edit Artifact",
                "Edit Artifact - Admin",
                "/admin/artifacts/" + artifactId,
                "/admin/artifacts/" + artifactId,
                title,
                sourceContent,
                normalizeStatus(status),
                true,
                errorMessage,
                csrfToken
        );
    }

    private String renderForm(
            String pageHeading,
            String pageTitle,
            String formAction,
            String cancelHref,
            String title,
            String sourceContent,
            String status,
            boolean showStatus,
            String errorMessage,
            String csrfToken
    ) {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <link rel="icon" href="data:,">
                    <title>%s</title>
                    <style>
                        body {
                            margin: 0;
                            background: #f5f5f2;
                            color: #242424;
                            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Helvetica Neue", sans-serif;
                            font-size: 15px;
                        }

                        header {
                            border-bottom: 1px solid #dedbd2;
                            background: #ffffff;
                        }

                        .shell {
                            max-width: 900px;
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

                        a {
                            color: #0f766e;
                            text-decoration-thickness: 1px;
                            text-underline-offset: 3px;
                        }

                        form {
                            margin-top: 24px;
                        }

                        label {
                            display: block;
                            margin: 0 0 8px;
                            color: #404040;
                            font-weight: 650;
                        }

                        input,
                        select,
                        textarea,
                        button {
                            border: 1px solid #c9c6bd;
                            border-radius: 6px;
                            background: #ffffff;
                            color: #242424;
                            font: inherit;
                        }

                        input,
                        select,
                        textarea {
                            width: 100%%;
                            box-sizing: border-box;
                            padding: 10px 12px;
                        }

                        input,
                        select {
                            height: 42px;
                        }

                        textarea {
                            min-height: 360px;
                            resize: vertical;
                            line-height: 1.6;
                            font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace;
                        }

                        .field {
                            margin-bottom: 18px;
                        }

                        .actions {
                            display: flex;
                            align-items: center;
                            gap: 12px;
                        }

                        button {
                            height: 40px;
                            padding: 0 16px;
                            border-color: #0f766e;
                            background: #0f766e;
                            color: #ffffff;
                            cursor: pointer;
                        }

                        .error {
                            margin: 0 0 18px;
                            padding: 10px 12px;
                            border: 1px solid #f1b4b4;
                            border-radius: 6px;
                            background: #fff5f5;
                            color: #991b1b;
                        }
                    </style>
                </head>
                <body>
                    <header>
                        <div class="shell topbar">
                            <h1>%s</h1>
                            <a href="/admin/artifacts">Back to list</a>
                        </div>
                    </header>
                    <main class="shell">
                        %s
                        <form method="post" action="%s">
                            <input type="hidden" name="_csrf" value="%s">
                            <div class="field">
                                <label for="title">Title</label>
                                <input id="title" name="title" value="%s" maxlength="200" required>
                            </div>
                            <div class="field">
                                <label for="sourceContent">Markdown</label>
                                <textarea id="sourceContent" name="sourceContent" required>%s</textarea>
                            </div>
                            %s
                            <div class="actions">
                                <button type="submit">Save</button>
                                <a href="%s">Cancel</a>
                            </div>
                        </form>
                    </main>
                </body>
                </html>
                """.formatted(
                escapeHtml(pageTitle),
                escapeHtml(pageHeading),
                renderError(errorMessage),
                escapeHtml(formAction),
                escapeHtml(csrfToken),
                escapeHtml(title),
                escapeHtml(sourceContent),
                renderStatusField(status, showStatus),
                escapeHtml(cancelHref)
        );
    }

    private String renderStatusField(String status, boolean showStatus) {
        if (!showStatus) {
            return "";
        }

        return """
                            <div class="field">
                                <label for="status">Status</label>
                                <select id="status" name="status" required>
                                    <option value="published"%s>Published</option>
                                    <option value="draft"%s>Draft</option>
                                </select>
                            </div>
                """.formatted(
                selected("published".equals(status)),
                selected("draft".equals(status))
        );
    }

    private String renderError(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return "";
        }
        return "<p class=\"error\">%s</p>".formatted(escapeHtml(errorMessage));
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "published";
        }
        return status.trim().toLowerCase(Locale.ROOT);
    }

    private String selected(boolean selected) {
        return selected ? " selected" : "";
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
