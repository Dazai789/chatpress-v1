package com.chatpress.v1.artifact.web;

import com.chatpress.v1.artifact.Artifact;
import com.chatpress.v1.artifact.ArtifactService;
import com.chatpress.v1.artifact.exception.InvalidMarkdownImportException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Locale;

@RestController
public class AdminArtifactController {

    private final ArtifactService artifactService;
    private final AdminArtifactPageRenderer adminArtifactPageRenderer;
    private final AdminArtifactFormRenderer adminArtifactFormRenderer;
    private final AdminArtifactDetailRenderer adminArtifactDetailRenderer;
    private final AdminArtifactDeleteRenderer adminArtifactDeleteRenderer;
    private final AdminMarkdownImportRenderer adminMarkdownImportRenderer;

    public AdminArtifactController(
            ArtifactService artifactService,
            AdminArtifactPageRenderer adminArtifactPageRenderer,
            AdminArtifactFormRenderer adminArtifactFormRenderer,
            AdminArtifactDetailRenderer adminArtifactDetailRenderer,
            AdminArtifactDeleteRenderer adminArtifactDeleteRenderer,
            AdminMarkdownImportRenderer adminMarkdownImportRenderer
    ) {
        this.artifactService = artifactService;
        this.adminArtifactPageRenderer = adminArtifactPageRenderer;
        this.adminArtifactFormRenderer = adminArtifactFormRenderer;
        this.adminArtifactDetailRenderer = adminArtifactDetailRenderer;
        this.adminArtifactDeleteRenderer = adminArtifactDeleteRenderer;
        this.adminMarkdownImportRenderer = adminMarkdownImportRenderer;
    }

    @GetMapping(value = "/admin/artifacts", produces = MediaType.TEXT_HTML_VALUE)
    public String listArtifacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status
    ) {
        Page<Artifact> artifacts = artifactService.listArtifacts(page, size, q, status, currentUsername());
        return adminArtifactPageRenderer.render(artifacts, q, status);
    }

    @GetMapping(value = "/admin/artifacts/new", produces = MediaType.TEXT_HTML_VALUE)
    public String newArtifactForm(HttpServletRequest request) {
        return adminArtifactFormRenderer.render("", "", null, csrfToken(request));
    }

    @GetMapping(value = "/admin/artifacts/import/markdown", produces = MediaType.TEXT_HTML_VALUE)
    public String importMarkdownForm(HttpServletRequest request) {
        return adminMarkdownImportRenderer.render("", null, csrfToken(request));
    }

    @GetMapping(value = "/admin/artifacts/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String getArtifact(@PathVariable Long id) {
        Artifact artifact = artifactService.getArtifactOrThrow(id, currentUsername());
        return adminArtifactDetailRenderer.render(artifact);
    }

    @GetMapping(value = "/admin/artifacts/{id}/edit", produces = MediaType.TEXT_HTML_VALUE)
    public String editArtifactForm(@PathVariable Long id, HttpServletRequest request) {
        Artifact artifact = artifactService.getArtifactOrThrow(id, currentUsername());
        return adminArtifactFormRenderer.renderEdit(artifact, null, csrfToken(request));
    }

    @GetMapping(value = "/admin/artifacts/{id}/delete", produces = MediaType.TEXT_HTML_VALUE)
    public String deleteArtifactForm(@PathVariable Long id, HttpServletRequest request) {
        Artifact artifact = artifactService.getArtifactOrThrow(id, currentUsername());
        return adminArtifactDeleteRenderer.render(artifact, csrfToken(request));
    }

    @PostMapping(
            value = "/admin/artifacts",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE
    )
    public ResponseEntity<String> createArtifact(
            @RequestParam String title,
            @RequestParam String sourceContent,
            HttpServletRequest request
    ) {
        if (title.isBlank() || sourceContent.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(adminArtifactFormRenderer.render(
                            title,
                            sourceContent,
                            "Title and Markdown are required",
                            csrfToken(request)
                    ));
        }

        artifactService.createArtifact(title.trim(), sourceContent, currentUsername());
        return ResponseEntity.status(303)
                .header(HttpHeaders.LOCATION, URI.create("/admin/artifacts").toString())
                .build();
    }

    @PostMapping(
            value = "/admin/artifacts/import/markdown",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_HTML_VALUE
    )
    public ResponseEntity<String> importMarkdown(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            HttpServletRequest request
    ) {
        try {
            Artifact artifact = artifactService.importMarkdownFile(file, title, currentUsername());
            return ResponseEntity.status(303)
                    .header(HttpHeaders.LOCATION, URI.create("/admin/artifacts/" + artifact.getId()).toString())
                    .build();
        } catch (InvalidMarkdownImportException exception) {
            return ResponseEntity.badRequest()
                    .body(adminMarkdownImportRenderer.render(title, exception.getMessage(), csrfToken(request)));
        }
    }

    @PostMapping(
            value = "/admin/artifacts/{id}",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE
    )
    public ResponseEntity<String> updateArtifact(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String sourceContent,
            @RequestParam String status,
            HttpServletRequest request
    ) {
        if (title.isBlank() || sourceContent.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(adminArtifactFormRenderer.renderEdit(
                            id,
                            title,
                            sourceContent,
                            status,
                            "Title and Markdown are required",
                            csrfToken(request)
                    ));
        }

        Artifact.Status artifactStatus = parseStatus(status);
        if (artifactStatus == null) {
            return ResponseEntity.badRequest()
                    .body(adminArtifactFormRenderer.renderEdit(
                            id,
                            title,
                            sourceContent,
                            status,
                            "Status must be draft or published",
                            csrfToken(request)
                    ));
        }

        artifactService.updateArtifactOrThrow(id, title.trim(), sourceContent, currentUsername());
        artifactService.updateArtifactStatusOrThrow(id, artifactStatus, currentUsername());
        return ResponseEntity.status(303)
                .header(HttpHeaders.LOCATION, URI.create("/admin/artifacts/" + id).toString())
                .build();
    }

    @PostMapping(
            value = "/admin/artifacts/{id}/delete",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public ResponseEntity<Void> deleteArtifact(@PathVariable Long id) {
        artifactService.deleteArtifactOrThrow(id, currentUsername());
        return ResponseEntity.status(303)
                .header(HttpHeaders.LOCATION, URI.create("/admin/artifacts").toString())
                .build();
    }

    private String csrfToken(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        return token != null ? token.getToken() : "";
    }

    private String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private Artifact.Status parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return Artifact.Status.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
