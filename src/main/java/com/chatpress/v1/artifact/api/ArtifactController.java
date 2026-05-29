package com.chatpress.v1.artifact.api;

import com.chatpress.v1.artifact.Artifact;
import com.chatpress.v1.artifact.ArtifactService;
import com.chatpress.v1.artifact.dto.ArtifactRequest;
import com.chatpress.v1.artifact.dto.ArtifactPageResponse;
import com.chatpress.v1.artifact.dto.ArtifactResponse;
import com.chatpress.v1.artifact.dto.ArtifactStatusRequest;
import com.chatpress.v1.artifact.dto.ArtifactSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/artifacts")
public class ArtifactController {

    private final ArtifactService artifactService;

    public ArtifactController(ArtifactService artifactService) {
        this.artifactService = artifactService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArtifactResponse createArtifact(@Valid @RequestBody ArtifactRequest request) {
        Artifact artifact = artifactService.createArtifact(
                request.title(),
                request.sourceContent(),
                currentUsername()
        );
        return ArtifactResponse.from(artifact);
    }

    @PostMapping(value = "/import/markdown", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ArtifactResponse importMarkdownFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title
    ) {
        return ArtifactResponse.from(artifactService.importMarkdownFile(file, title, currentUsername()));
    }

    @GetMapping
    public ArtifactPageResponse<ArtifactSummaryResponse> listArtifacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status
    ) {
        return ArtifactPageResponse.from(
                artifactService.listArtifacts(page, size, q, status, currentUsername())
                        .map(ArtifactSummaryResponse::from)
        );
    }

    @GetMapping("/{id}")
    public ArtifactResponse getArtifact(@PathVariable Long id) {
        return ArtifactResponse.from(artifactService.getArtifactOrThrow(id, currentUsername()));
    }

    @PutMapping("/{id}")
    public ArtifactResponse updateArtifact(
            @PathVariable Long id,
            @Valid @RequestBody ArtifactRequest request
    ) {
        Artifact artifact = artifactService.updateArtifactOrThrow(
                id,
                request.title(),
                request.sourceContent(),
                currentUsername()
        );
        return ArtifactResponse.from(artifact);
    }

    @PutMapping("/{id}/status")
    public ArtifactResponse updateArtifactStatus(
            @PathVariable Long id,
            @Valid @RequestBody ArtifactStatusRequest request
    ) {
        Artifact artifact = artifactService.updateArtifactStatusOrThrow(id, request.toArtifactStatus(), currentUsername());
        return ArtifactResponse.from(artifact);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArtifact(@PathVariable Long id) {
        artifactService.deleteArtifactOrThrow(id, currentUsername());
        return ResponseEntity.noContent().build();
    }

    private String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
