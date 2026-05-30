package com.chatpress.artifact;

import com.chatpress.artifact.renderer.PublicListRenderer;
import com.chatpress.artifact.renderer.PublicPageRenderer;

import com.chatpress.artifact.ArtifactService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicPageController {

    private final ArtifactService artifactService;
    private final ArtifactMapper artifactMapper;
    private final PublicPageRenderer publicPageRenderer;
    private final PublicListRenderer publicListRenderer;
    private final PublicPageCache cache;

    public PublicPageController(ArtifactService artifactService,
                                ArtifactMapper artifactMapper,
                                PublicPageRenderer publicPageRenderer,
                                PublicListRenderer publicListRenderer,
                                PublicPageCache cache) {
        this.artifactService = artifactService;
        this.artifactMapper = artifactMapper;
        this.publicPageRenderer = publicPageRenderer;
        this.publicListRenderer = publicListRenderer;
        this.cache = cache;
    }

    @GetMapping(value = "/p", produces = MediaType.TEXT_HTML_VALUE)
    public String listPublished(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        var result = artifactMapper.findPublishedByPage(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<Artifact>(page + 1, size)); // MyBatis-Plus 1-based
        var artifacts = new org.springframework.data.domain.PageImpl<>(
                result.getRecords(),
                org.springframework.data.domain.PageRequest.of(page, size),
                result.getTotal()
        );
        return publicListRenderer.render(artifacts);
    }

    @GetMapping(value = "/p/{slug}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getPublicPage(@PathVariable String slug) {
        String cached = cache.get(slug);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }

        return artifactService.getPublishedArtifactBySlug(slug)
                .map(artifact -> {
                    String html = publicPageRenderer.render(artifact);
                    cache.put(slug, html);
                    return ResponseEntity.ok(html);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
