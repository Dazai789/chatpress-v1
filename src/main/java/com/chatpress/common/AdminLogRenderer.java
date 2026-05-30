package com.chatpress.common;

import org.springframework.data.domain.Page;
import com.chatpress.common.HtmlUtils;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class AdminLogRenderer {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String render(Page<OperationLog> logs) {
        StringBuilder rows = new StringBuilder();
        for (OperationLog log : logs.getContent()) {
            rows.append("""
                    <tr>
                        <td>%s</td>
                        <td><span class="action">%s</span></td>
                        <td class="target">%s</td>
                        <td>%d ms</td>
                        <td>%s</td>
                    </tr>
                    """.formatted(
                    HtmlUtils.escapeHtml(log.getUsername()),
                    HtmlUtils.escapeHtml(log.getAction()),
                    HtmlUtils.escapeHtml(log.getTarget()),
                    log.getDurationMs(),
                    log.getCreatedAt().format(FORMATTER)
            ));
        }

        if (rows.isEmpty()) {
            rows.append("""
                    <tr>
                        <td colspan="5" class="empty">No operation logs yet</td>
                    </tr>
                    """);
        }

        return """
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <link rel="icon" href="data:,">
                    <title>Operation Logs - Admin</title>
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
                            max-width: 1080px;
                            margin: 0 auto;
                            padding: 24px;
                        }

                        h1 {
                            margin: 0;
                            font-size: 1.5rem;
                            font-weight: 650;
                        }

                        .topbar {
                            display: flex;
                            align-items: center;
                            justify-content: space-between;
                            gap: 16px;
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
                            color: #242424;
                        }

                        table {
                            width: 100%%;
                            border-collapse: collapse;
                            background: #ffffff;
                            border: 1px solid #dedbd2;
                            margin-top: 20px;
                        }

                        th, td {
                            padding: 12px 14px;
                            border-bottom: 1px solid #ebe8df;
                            text-align: left;
                            vertical-align: middle;
                        }

                        th {
                            background: #eeece4;
                            color: #4a4a4a;
                            font-size: 0.82rem;
                            font-weight: 650;
                            text-transform: uppercase;
                        }

                        tr:last-child td {
                            border-bottom: 0;
                        }

                        .target {
                            max-width: 360px;
                            overflow: hidden;
                            text-overflow: ellipsis;
                            white-space: nowrap;
                            font-size: 0.88rem;
                            color: #525252;
                        }

                        .action {
                            display: inline-block;
                            min-width: 100px;
                            padding: 3px 8px;
                            border-radius: 999px;
                            background: #e7e5ef;
                            color: #4c3f91;
                            font-size: 0.82rem;
                            text-align: center;
                        }

                        .action:has-text("DELETE") {
                            background: #fce4e4;
                            color: #991b1b;
                        }

                        .empty {
                            color: #737373;
                        }

                        .pager {
                            display: flex;
                            align-items: center;
                            justify-content: space-between;
                            gap: 12px;
                            margin-top: 16px;
                            color: #525252;
                        }

                        .pager nav {
                            display: flex;
                            gap: 8px;
                        }

                        .pager a, .pager span.link-disabled {
                            display: inline-flex;
                            align-items: center;
                            height: 36px;
                            padding: 0 12px;
                            border: 1px solid #c9c6bd;
                            border-radius: 6px;
                            background: #ffffff;
                            text-decoration: none;
                        }

                        .pager span.link-disabled {
                            color: #a3a3a3;
                        }

                        @media (max-width: 760px) {
                            .shell {
                                padding: 18px;
                            }

                            .topbar {
                                align-items: flex-start;
                                flex-direction: column;
                            }

                            table {
                                display: block;
                                overflow-x: auto;
                            }
                        }
                    </style>
                </head>
                <body>
                    <header>
                        <div class="shell topbar">
                            <h1>Operation Logs</h1>
                            <a class="secondary-link" href="/admin/artifacts">Back to Artifacts</a>
                        </div>
                    </header>
                    <main class="shell">
                        <table>
                            <thead>
                                <tr>
                                    <th>User</th>
                                    <th>Action</th>
                                    <th>Target</th>
                                    <th>Duration</th>
                                    <th>Time</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>
                        <div class="pager">
                            <span>Page %d of %d · %d total</span>
                            <nav>
                                %s
                                %s
                            </nav>
                        </div>
                    </main>
                </body>
                </html>
                """.formatted(
                rows.toString(),
                logs.getNumber() + 1,
                Math.max(logs.getTotalPages(), 1),
                logs.getTotalElements(),
                previousLink(logs),
                nextLink(logs)
        );
    }

    private String previousLink(Page<OperationLog> logs) {
        if (!logs.hasPrevious()) {
            return "<span class=\"link-disabled\">Previous</span>";
        }
        return "<a href=\"/admin/logs?page=%d\">Previous</a>".formatted(logs.getNumber() - 1);
    }

    private String nextLink(Page<OperationLog> logs) {
        if (!logs.hasNext()) {
            return "<span class=\"link-disabled\">Next</span>";
        }
        return "<a href=\"/admin/logs?page=%d\">Next</a>".formatted(logs.getNumber() + 1);
    }
}
