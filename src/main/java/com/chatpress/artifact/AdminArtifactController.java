package com.chatpress.artifact;

import com.chatpress.artifact.renderer.AdminPageRenderer;
import com.chatpress.artifact.renderer.AdminFormRenderer;
import com.chatpress.artifact.renderer.AdminDetailRenderer;
import com.chatpress.artifact.renderer.AdminDeleteRenderer;
import com.chatpress.artifact.renderer.AdminMarkdownImportRenderer;

import com.chatpress.artifact.Artifact;
import com.chatpress.artifact.ArtifactService;
import com.chatpress.artifact.exception.ArtifactNotFoundException;
import com.chatpress.artifact.exception.InvalidMarkdownImportException;
import com.chatpress.common.AdminLogRenderer;
import com.chatpress.common.OperationLog;
import com.chatpress.common.OperationLogRepository;
import com.chatpress.common.SecurityUtils;
import com.chatpress.common.annotation.LogOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;


@RestController
public class AdminArtifactController {

    private static final Logger log = LoggerFactory.getLogger(AdminArtifactController.class);

    private final ArtifactService artifactService;
    private final AdminPageRenderer adminPageRenderer;
    private final AdminFormRenderer adminFormRenderer;
    private final AdminDetailRenderer adminDetailRenderer;
    private final AdminDeleteRenderer adminDeleteRenderer;
    private final AdminMarkdownImportRenderer adminMarkdownImportRenderer;
    private final AdminLogRenderer adminLogRenderer;
    private final OperationLogRepository operationLogRepository;

    public AdminArtifactController(
            ArtifactService artifactService,
            AdminPageRenderer adminPageRenderer,
            AdminFormRenderer adminFormRenderer,
            AdminDetailRenderer adminDetailRenderer,
            AdminDeleteRenderer adminDeleteRenderer,
            AdminMarkdownImportRenderer adminMarkdownImportRenderer,
            AdminLogRenderer adminLogRenderer,
            OperationLogRepository operationLogRepository
    ) {
        this.artifactService = artifactService;
        this.adminPageRenderer = adminPageRenderer;
        this.adminFormRenderer = adminFormRenderer;
        this.adminDetailRenderer = adminDetailRenderer;
        this.adminDeleteRenderer = adminDeleteRenderer;
        this.adminMarkdownImportRenderer = adminMarkdownImportRenderer;
        this.adminLogRenderer = adminLogRenderer;
        this.operationLogRepository = operationLogRepository;
    }

    @GetMapping(value = {"/admin", "/admin/"})
    public ResponseEntity<Void> adminIndex() {
        return ResponseEntity.status(303)
                .header(HttpHeaders.LOCATION, URI.create("/admin/artifacts").toString())
                .build();
    }

    @GetMapping(value = "/admin/logs", produces = MediaType.TEXT_HTML_VALUE)
    public String listLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<OperationLog> logs = operationLogRepository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(page, size));
        return adminLogRenderer.render(logs);
    }

    @GetMapping(value = "/admin/artifacts", produces = MediaType.TEXT_HTML_VALUE)
    public String listArtifacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tag
    ) {
        var result = artifactService.listArtifacts(page, size, q, status, tag, SecurityUtils.currentUsername());
        var artifacts = new org.springframework.data.domain.PageImpl<>(
                result.getRecords(),
                org.springframework.data.domain.PageRequest.of(page, size),
                result.getTotal()
        );
        return adminPageRenderer.render(artifacts, q, status, tag);
    }

    @GetMapping(value = "/admin/artifacts/new", produces = MediaType.TEXT_HTML_VALUE)
    public String newArtifactForm(HttpServletRequest request) {
        return adminFormRenderer.render("", "", "", null, SecurityUtils.csrfToken(request));
    }

    @GetMapping(value = "/admin/artifacts/import/markdown", produces = MediaType.TEXT_HTML_VALUE)
    public String importMarkdownForm(HttpServletRequest request) {
        return adminMarkdownImportRenderer.render("", null, SecurityUtils.csrfToken(request));
    }

    @GetMapping(value = "/admin/artifacts/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String getArtifact(@PathVariable Long id) {
        Artifact artifact = artifactService.getArtifactOrThrow(id, SecurityUtils.currentUsername());
        return adminDetailRenderer.render(artifact);
    }

    @GetMapping(value = "/admin/artifacts/{id}/edit", produces = MediaType.TEXT_HTML_VALUE)
    public String editArtifactForm(@PathVariable Long id, HttpServletRequest request) {
        Artifact artifact = artifactService.getArtifactOrThrow(id, SecurityUtils.currentUsername());
        return adminFormRenderer.renderEdit(artifact, null, SecurityUtils.csrfToken(request));
    }

    @GetMapping(value = "/admin/artifacts/{id}/delete", produces = MediaType.TEXT_HTML_VALUE)
    public String deleteArtifactForm(@PathVariable Long id, HttpServletRequest request) {
        Artifact artifact = artifactService.getArtifactOrThrow(id, SecurityUtils.currentUsername());
        return adminDeleteRenderer.render(artifact, SecurityUtils.csrfToken(request));
    }

    @LogOperation("CREATE_ARTIFACT")
    @PostMapping(
            value = "/admin/artifacts",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE
    )
    public ResponseEntity<String> createArtifact(
            @RequestParam String title,
            @RequestParam String sourceContent,
            @RequestParam(required = false) String tags,
            HttpServletRequest request
    ) {
        if (title.isBlank() || sourceContent.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(adminFormRenderer.render(
                            title,
                            sourceContent,
                            tags,
                            "Title and Markdown are required",
                            SecurityUtils.csrfToken(request)
                    ));
        }

        artifactService.createArtifact(title.trim(), sourceContent, parseTagNames(tags), SecurityUtils.currentUsername());
        return ResponseEntity.status(303)
                .header(HttpHeaders.LOCATION, URI.create("/admin/artifacts").toString())
                .build();
    }

    @LogOperation("IMPORT_MARKDOWN")
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
            Artifact artifact = artifactService.importMarkdownFile(file, title, SecurityUtils.currentUsername());
            return ResponseEntity.status(303)
                    .header(HttpHeaders.LOCATION, URI.create("/admin/artifacts/" + artifact.getId()).toString())
                    .build();
        } catch (InvalidMarkdownImportException exception) {
            return ResponseEntity.badRequest()
                    .body(adminMarkdownImportRenderer.render(title, exception.getMessage(), SecurityUtils.csrfToken(request)));
        }
    }

    @LogOperation("UPDATE_ARTIFACT")
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
            @RequestParam(required = false) String tags,
            HttpServletRequest request
    ) {
        if (title.isBlank() || sourceContent.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(adminFormRenderer.renderEdit(
                            id,
                            title,
                            sourceContent,
                            status,
                            tags,
                            "Title and Markdown are required",
                            SecurityUtils.csrfToken(request)
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
                            tags,
                            "Status must be draft or published",
                            SecurityUtils.csrfToken(request)
                    ));
        }

        artifactService.updateArtifactWithStatusOrThrow(id, title.trim(), sourceContent, parseTagNames(tags), artifactStatus, SecurityUtils.currentUsername());
        return ResponseEntity.status(303)
                .header(HttpHeaders.LOCATION, URI.create("/admin/artifacts/" + id).toString())
                .build();
    }

    @LogOperation("DELETE_ARTIFACT")
    @PostMapping(
            value = "/admin/artifacts/{id}/delete",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public ResponseEntity<Void> deleteArtifact(@PathVariable Long id) {
        artifactService.deleteArtifactOrThrow(id, SecurityUtils.currentUsername());
        return ResponseEntity.status(303)
                .header(HttpHeaders.LOCATION, URI.create("/admin/artifacts").toString())
                .build();
    }

    private Artifact.Status parseStatus(String status) {
        return Artifact.Status.fromString(status).orElse(null);
    }

    private List<String> parseTagNames(String tags) {
        if (tags == null || tags.isBlank()) {
            return null;
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @ExceptionHandler(ArtifactNotFoundException.class)
    public ResponseEntity<String> handleArtifactNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.TEXT_HTML)
                .body("""
                        <!doctype html>
                        <html lang="en">
                        <head>
                            <meta charset="utf-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1">
                            <title>Not Found - Admin</title>
                            <style>
                                body {
                                    margin: 0; padding: 56px 20px;
                                    background: #f5f5f2; color: #242424;
                                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                                    text-align: center;
                                }
                                a { color: #0f766e; }
                            </style>
                        </head>
                        <body>
                            <h1>404</h1>
                            <p>Artifact not found.</p>
                            <p><a href="/admin/artifacts">Back to list</a></p>
                        </body>
                        </html>
                        """);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpected(Exception exception) {
        log.error("Unexpected admin error: {}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_HTML)
                .body("""
                        <!doctype html>
                        <html lang="en">
                        <head>
                            <meta charset="utf-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1">
                            <title>Error - Admin</title>
                            <style>
                                body {
                                    margin: 0; padding: 56px 20px;
                                    background: #f5f5f2; color: #242424;
                                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                                    text-align: center;
                                }
                                a { color: #0f766e; }
                            </style>
                        </head>
                        <body>
                            <h1>500</h1>
                            <p>An unexpected error occurred. Please try again.</p>
                            <p><a href="/admin/artifacts">Back to list</a></p>
                        </body>
                        </html>
                        """);
    }
}
