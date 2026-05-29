package com.chatpress.artifact;

import com.chatpress.artifact.renderer.AdminPageRenderer;
import com.chatpress.artifact.renderer.AdminFormRenderer;
import com.chatpress.artifact.renderer.AdminDetailRenderer;
import com.chatpress.artifact.renderer.AdminDeleteRenderer;
import com.chatpress.artifact.renderer.AdminMarkdownImportRenderer;

import com.chatpress.artifact.Artifact;
import com.chatpress.artifact.ArtifactService;
import com.chatpress.artifact.exception.InvalidMarkdownImportException;
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


@RestController
public class AdminArtifactController {

    private final ArtifactService artifactService;
    private final AdminPageRenderer adminPageRenderer;
    private final AdminFormRenderer adminFormRenderer;
    private final AdminDetailRenderer adminDetailRenderer;
    private final AdminDeleteRenderer adminDeleteRenderer;
    private final AdminMarkdownImportRenderer adminMarkdownImportRenderer;

    public AdminArtifactController(
            ArtifactService artifactService,
            AdminPageRenderer adminPageRenderer,
            AdminFormRenderer adminFormRenderer,
            AdminDetailRenderer adminDetailRenderer,
            AdminDeleteRenderer adminDeleteRenderer,
            AdminMarkdownImportRenderer adminMarkdownImportRenderer
    ) {
        this.artifactService = artifactService;
        this.adminPageRenderer = adminPageRenderer;
        this.adminFormRenderer = adminFormRenderer;
        this.adminDetailRenderer = adminDetailRenderer;
        this.adminDeleteRenderer = adminDeleteRenderer;
        this.adminMarkdownImportRenderer = adminMarkdownImportRenderer;
    }

    @GetMapping(value = {"/admin", "/admin/"})
    public ResponseEntity<Void> adminIndex() {
        return ResponseEntity.status(303)
                .header(HttpHeaders.LOCATION, URI.create("/admin/artifacts").toString())
                .build();
    }

    @GetMapping(value = "/admin/artifacts", produces = MediaType.TEXT_HTML_VALUE)
    public String listArtifacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status
    ) {
        Page<Artifact> artifacts = artifactService.listArtifacts(page, size, q, status, currentUsername());
        return adminPageRenderer.render(artifacts, q, status);
    }

    @GetMapping(value = "/admin/artifacts/new", produces = MediaType.TEXT_HTML_VALUE)
    public String newArtifactForm(HttpServletRequest request) {
        return adminFormRenderer.render("", "", null, csrfToken(request));
    }

    @GetMapping(value = "/admin/artifacts/import/markdown", produces = MediaType.TEXT_HTML_VALUE)
    public String importMarkdownForm(HttpServletRequest request) {
        return adminMarkdownImportRenderer.render("", null, csrfToken(request));
    }

    @GetMapping(value = "/admin/artifacts/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String getArtifact(@PathVariable Long id) {
        Artifact artifact = artifactService.getArtifactOrThrow(id, currentUsername());
        return adminDetailRenderer.render(artifact);
    }

    @GetMapping(value = "/admin/artifacts/{id}/edit", produces = MediaType.TEXT_HTML_VALUE)
    public String editArtifactForm(@PathVariable Long id, HttpServletRequest request) {
        Artifact artifact = artifactService.getArtifactOrThrow(id, currentUsername());
        return adminFormRenderer.renderEdit(artifact, null, csrfToken(request));
    }

    @GetMapping(value = "/admin/artifacts/{id}/delete", produces = MediaType.TEXT_HTML_VALUE)
    public String deleteArtifactForm(@PathVariable Long id, HttpServletRequest request) {
        Artifact artifact = artifactService.getArtifactOrThrow(id, currentUsername());
        return adminDeleteRenderer.render(artifact, csrfToken(request));
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
                    .body(adminFormRenderer.render(
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
                    .body(adminFormRenderer.renderEdit(
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
                    .body(adminFormRenderer.renderEdit(
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
        return Artifact.Status.fromString(status).orElse(null);
    }
}
