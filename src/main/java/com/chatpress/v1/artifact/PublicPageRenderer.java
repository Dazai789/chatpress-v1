package com.chatpress.v1.artifact;

import org.springframework.stereotype.Component;

@Component
public class PublicPageRenderer {

    public String render(Artifact artifact) {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>%s</title>
                    <style>
                        body {
                            margin: 0;
                            padding: 48px 20px;
                            background: #fafafa;
                            color: #1f2933;
                            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                            line-height: 1.7;
                        }

                        main {
                            max-width: 760px;
                            margin: 0 auto;
                        }

                        h1, h2, h3 {
                            line-height: 1.25;
                        }

                        code {
                            background: #f1f5f9;
                            padding: 2px 5px;
                            border-radius: 4px;
                        }

                        pre {
                            overflow-x: auto;
                            background: #111827;
                            color: #f9fafb;
                            padding: 16px;
                        }

                        img {
                            max-width: 100%%;
                        }

                        @media (max-width: 640px) {
                            body {
                                padding: 24px 12px;
                            }
                        }
                    </style>
                </head>
                <body>
                    <main>
                        %s
                    </main>
                </body>
                </html>
                """.formatted(escapeHtml(artifact.getTitle()), artifact.getRenderedHtml());
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
