package com.chatpress.v1.artifact;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

@Component
public class MarkdownRenderer {

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().build();

    public String render(String markdown) {
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}
