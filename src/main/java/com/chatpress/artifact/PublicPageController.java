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
    private final ArtifactRepository artifactRepository;
    private final PublicPageRenderer publicPageRenderer;
    private final PublicListRenderer publicListRenderer;
    private final PublicPageCache cache;

    public PublicPageController(ArtifactService artifactService,
                                ArtifactRepository artifactRepository,
                                PublicPageRenderer publicPageRenderer,
                                PublicListRenderer publicListRenderer,
                                PublicPageCache cache) {
        this.artifactService = artifactService;
        this.artifactRepository = artifactRepository;
        this.publicPageRenderer = publicPageRenderer;
        this.publicListRenderer = publicListRenderer;
        this.cache = cache;
    }

    @GetMapping(value = "/p", produces = MediaType.TEXT_HTML_VALUE)
    public String listPublished(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        Page<Artifact> artifacts = artifactRepository.findByStatusOrderByCreatedAtDesc(
                Artifact.Status.PUBLISHED,
                PageRequest.of(page, size));
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
