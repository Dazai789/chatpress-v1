package com.chatpress.v1.artifact;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownRendererTest {

    private final MarkdownRenderer markdownRenderer = new MarkdownRenderer();

    @Test
    void renderBasicMarkdown() {
        String html = markdownRenderer.render("""
                # Title

                ## Section

                Paragraph with `inline code`.

                - One
                - Two

                > Quote

                ```
                System.out.println("hello");
                ```

                [Spring](https://spring.io)
                """);

        assertThat(html).contains("<h1>Title</h1>");
        assertThat(html).contains("<h2>Section</h2>");
        assertThat(html).contains("<p>Paragraph with <code>inline code</code>.</p>");
        assertThat(html).contains("<li>One</li>");
        assertThat(html).contains("<blockquote>");
        assertThat(html).contains("<pre><code>System.out.println(\"hello\");");
        assertThat(html).contains("<a href=\"https://spring.io\">Spring</a>");
    }

    @Test
    void renderTable() {
        String html = markdownRenderer.render("""
                | Name | Role |
                |---|---|
                | Spring | Backend |
                """);

        assertThat(html).contains("<table>");
        assertThat(html).contains("<th>Name</th>");
        assertThat(html).contains("<td>Spring</td>");
    }

    @Test
    void renderStrikethrough() {
        String html = markdownRenderer.render("Use ~~old~~ new text.");

        assertThat(html).contains("<del>old</del>");
    }

    @Test
    void renderAutolink() {
        String html = markdownRenderer.render("Visit https://spring.io for docs.");

        assertThat(html).contains("<a href=\"https://spring.io\">https://spring.io</a>");
    }

    @Test
    void renderTaskList() {
        String html = markdownRenderer.render("""
                - [x] Done
                - [ ] Todo
                """);

        assertThat(html).contains("type=\"checkbox\"");
        assertThat(html).contains("checked");
        assertThat(html).contains("disabled");
        assertThat(html).contains("Done");
        assertThat(html).contains("Todo");
    }

    @Test
    void removeUnsafeHtml() {
        String html = markdownRenderer.render("""
                # Safe Title

                <script>alert("xss")</script>
                <p onclick="alert('xss')">Safe paragraph</p>
                <img src="https://example.com/image.png" onerror="alert('xss')">
                <a href="javascript:alert('xss')">Bad link</a>
                """);

        assertThat(html).contains("<h1>Safe Title</h1>");
        assertThat(html).contains("<p>Safe paragraph</p>");
        assertThat(html).contains("<img src=\"https://example.com/image.png\">");
        assertThat(html).contains("<a>Bad link</a>");
        assertThat(html).doesNotContain("<script>");
        assertThat(html).doesNotContain("onclick");
        assertThat(html).doesNotContain("onerror");
        assertThat(html).doesNotContain("javascript:");
    }
}
