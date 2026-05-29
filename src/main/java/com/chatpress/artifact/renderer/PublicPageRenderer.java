package com.chatpress.artifact.renderer;

import com.chatpress.artifact.Artifact;

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
                            padding: 56px 20px 72px;
                            background: #f7f7f4;
                            color: #242424;
                            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Helvetica Neue", sans-serif;
                            font-size: 17px;
                            line-height: 1.75;
                        }

                        main {
                            max-width: 780px;
                            margin: 0 auto;
                        }

                        h1,
                        h2,
                        h3 {
                            color: #171717;
                            line-height: 1.25;
                            letter-spacing: 0;
                        }

                        h1 {
                            margin: 0 0 28px;
                            padding-bottom: 18px;
                            border-bottom: 1px solid #dedbd2;
                            font-size: 2.3rem;
                        }

                        h2 {
                            margin-top: 44px;
                            margin-bottom: 14px;
                            font-size: 1.45rem;
                        }

                        h3 {
                            margin-top: 32px;
                            margin-bottom: 10px;
                            font-size: 1.18rem;
                        }

                        p,
                        ul,
                        ol,
                        blockquote,
                        pre {
                            margin-top: 0;
                            margin-bottom: 20px;
                        }

                        ul,
                        ol {
                            padding-left: 1.4rem;
                        }

                        li + li {
                            margin-top: 6px;
                        }

                        a {
                            color: #0f766e;
                            text-decoration-thickness: 1px;
                            text-underline-offset: 3px;
                        }

                        a:hover {
                            color: #115e59;
                        }

                        blockquote {
                            margin-left: 0;
                            padding: 2px 0 2px 18px;
                            border-left: 4px solid #d4a72c;
                            color: #525252;
                            background: #fbfaf5;
                        }

                        code {
                            background: #ece7dc;
                            padding: 2px 5px;
                            border-radius: 4px;
                            font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace;
                            font-size: 0.92em;
                        }

                        pre {
                            overflow-x: auto;
                            background: #1f1f1f;
                            color: #f5f5f5;
                            padding: 18px;
                            border-radius: 6px;
                            line-height: 1.6;
                        }

                        pre code {
                            background: transparent;
                            padding: 0;
                            color: inherit;
                        }

                        table {
                            width: 100%%;
                            border-collapse: collapse;
                            margin: 24px 0;
                            font-size: 0.95rem;
                        }

                        th,
                        td {
                            padding: 10px 12px;
                            border: 1px solid #dedbd2;
                            text-align: left;
                        }

                        th {
                            background: #ece7dc;
                        }

                        img {
                            max-width: 100%%;
                            height: auto;
                        }

                        @media (max-width: 640px) {
                            body {
                                padding: 32px 16px 48px;
                                font-size: 16px;
                            }

                            h1 {
                                font-size: 1.85rem;
                            }

                            h2 {
                                font-size: 1.3rem;
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
